package com.glyphsense.app.glyph

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.glyphsense.app.data.NotificationStore
import com.glyphsense.app.data.PrioritySettingsRepository
import com.glyphsense.app.data.StoredNotification
import com.glyphsense.app.domain.DeviceProfile
import com.glyphsense.app.domain.PendingNotification
import com.glyphsense.app.domain.PriorityLevel
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphToy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class GlyphSenseToyService : Service() {

    private lateinit var scope: CoroutineScope
    private lateinit var renderer: FrameRenderer
    private lateinit var priorityRepo: PrioritySettingsRepository

    private var manager: GlyphMatrixManager? = null
    private var profile: DeviceProfile? = null
    private var renderJob: Job? = null
    private var autoCycleJob: Job? = null

    private val mode = MutableStateFlow(ToyMode.AppIcon)

    private val handler = Handler(Looper.getMainLooper(), Handler.Callback { msg: Message ->
        if (msg.what == GlyphToy.MSG_GLYPH_TOY) {
            val event = msg.data?.getString(GlyphToy.MSG_GLYPH_TOY_DATA)
            handleEvent(event)
            true
        } else {
            false
        }
    })
    private val messenger = Messenger(handler)

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        renderer = FrameRenderer(applicationContext)
        priorityRepo = PrioritySettingsRepository(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? {
        startToySession()
        return messenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        endToySession()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::scope.isInitialized) scope.cancel()
    }

    private fun startToySession() {
        mode.value = ToyMode.AppIcon

        val mgr = GlyphMatrixManager.getInstance(applicationContext)
        manager = mgr
        mgr.init(object : GlyphMatrixManager.Callback {
            override fun onServiceConnected(name: ComponentName?) {
                val active = DeviceProfile.SupportedDevices.firstOrNull { candidate ->
                    runCatching { mgr.register(candidate.deviceId) }.getOrDefault(false)
                }
                if (active == null) {
                    Log.e(TAG, "No supported Glyph Matrix device found")
                    return
                }
                profile = active
                renderJob = scope.launch { runRenderLoop(active, mgr) }
                if (!active.supportsTouch) {
                    autoCycleJob = scope.launch { runAutoCycle() }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                renderJob?.cancel()
                autoCycleJob?.cancel()
            }
        })
    }

    private fun endToySession() {
        renderJob?.cancel()
        autoCycleJob?.cancel()
        renderJob = null
        autoCycleJob = null
        runCatching { manager?.closeAppMatrix() }
        runCatching { manager?.unInit() }
        manager = null
        profile = null
    }

    private suspend fun runRenderLoop(profile: DeviceProfile, mgr: GlyphMatrixManager) {
        val transitioner = FrameTransitioner()
        viewFlow().collectLatest { view ->
            val target = renderer.render(view, profile)
            transitioner.transitionTo(target) { frame ->
                try {
                    mgr.setAppMatrixFrame(frame)
                } catch (t: Throwable) {
                    Log.e(TAG, "setAppMatrixFrame failed", t)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun viewFlow(): Flow<ToyView> {
        val enriched: Flow<List<PendingNotification>> =
            combine(NotificationStore.notifications, priorityRepo.priorities) { notifs, priorities ->
                notifs.enrich(priorities)
            }
        return combine(enriched, mode) { notifs, currentMode -> notifs to currentMode }
            .flatMapLatest { (notifs, currentMode) -> buildViewFlow(notifs, currentMode) }
    }

    private fun List<StoredNotification>.enrich(
        priorities: Map<String, PriorityLevel>
    ): List<PendingNotification> = this.mapNotNull { stored ->
        val p = priorities[stored.packageName] ?: PriorityLevel.DEFAULT
        if (p == PriorityLevel.SILENT) null
        else PendingNotification(
            key = stored.key,
            packageName = stored.packageName,
            priority = p,
            postedAt = stored.postedAt
        )
    }

    private fun buildViewFlow(
        notifs: List<PendingNotification>,
        currentMode: ToyMode
    ): Flow<ToyView> {
        if (notifs.isEmpty()) return flowOf(ToyView.Empty)

        return when (currentMode) {
            ToyMode.Pile -> flowOf(ToyView.Pile(notifs))
            ToyMode.AppIcon -> {
                val important = notifs
                    .filter { it.priority == PriorityLevel.IMPORTANT }
                    .sortedByDescending { it.postedAt }
                val uniquePackages = important.map { it.packageName }.distinct()

                when {
                    uniquePackages.isEmpty() -> flowOf(ToyView.Pile(notifs))
                    uniquePackages.size == 1 -> flowOf(
                        ToyView.AppIcon(uniquePackages.first(), totalImportant = 1)
                    )
                    else -> flow {
                        var i = 0
                        while (true) {
                            emit(
                                ToyView.AppIcon(
                                    packageName = uniquePackages[i % uniquePackages.size],
                                    totalImportant = uniquePackages.size
                                )
                            )
                            delay(ICON_CYCLE_MS)
                            i++
                        }
                    }
                }
            }
        }
    }

    private suspend fun runAutoCycle() {
        while (true) {
            delay(AUTO_CYCLE_MS)
            mode.value = mode.value.next()
        }
    }

    private fun handleEvent(event: String?) {
        when (event) {
            GlyphToy.EVENT_CHANGE -> {
                val supportsTouch = profile?.supportsTouch ?: false
                if (supportsTouch) mode.value = mode.value.next()
            }
        }
    }

    private companion object {
        const val TAG = "GlyphSense.Toy"
        const val ICON_CYCLE_MS = 1_500L
        const val AUTO_CYCLE_MS = 4_000L
    }
}
