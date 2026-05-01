package com.glyphsense.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

class InstalledAppsRepository(context: Context) {

    private val packageManager: PackageManager = context.applicationContext.packageManager

    suspend fun loadAll(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val flags = PackageManager.ResolveInfoFlags.of(0L)
        val resolved: List<ResolveInfo> = packageManager.queryIntentActivities(intent, flags)

        resolved
            .asSequence()
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .map { info ->
                InstalledApp(
                    packageName = info.packageName,
                    label = info.loadLabel(packageManager).toString(),
                    icon = info.loadIcon(packageManager)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun iconFor(packageName: String): Drawable? = try {
        packageManager.getApplicationIcon(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    fun labelFor(packageName: String): String = try {
        val info = packageManager.getApplicationInfo(
            packageName,
            PackageManager.ApplicationInfoFlags.of(0L)
        )
        info.loadLabel(packageManager).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        packageName
    }
}
