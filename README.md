# GlyphSense

A Nothing Glyph Matrix toy that turns your pending notifications into a glanceable display on the back of your phone — so you can decide whether to unlock without ever picking it up.

## What it does

Press the **Glyph Button** on the back of your Nothing Phone. The matrix lights up and shows you, at a glance, whether anything important is waiting:

- **Important apps with pending notifications** appear as cycling app glyphs — you see *who* wants you.
- **Long-press the Glyph Button** to switch to a "pile" view that shows the total volume of pending notifications, sorted by priority.
- **Apps marked as Silent** never appear on the matrix. Spam stays out of sight.

The classification is yours. Open the app, pick which of your installed apps are Important, Normal, or Silent. Changes update the toy in real time — no need to wait for a fresh notification.

## Supported devices

| Device | Matrix | Mode switching |
|---|---|---|
| Nothing Phone (3) | 25×25 | Long-press the Glyph Button |
| Nothing Phone (4a) Pro | 13×13 | Auto-cycles every 4 seconds (no Glyph Touch) |

The app installs and runs on any Android 13+ device, but Glyph rendering only activates on the two phones above. On unsupported devices the settings UI still works — the toy service simply stays inert.

## Two views

| View | What it shows | Trigger |
|---|---|---|
| **App Icon** (default) | Glyph of the most recent Important app, cycling every 1.5s if multiple | Toy activation |
| **Pile** | All pending notifications stacked by priority — Important bricks bright, Normal bricks dim | Long-press (Phone 3) or auto after 4s (Phone 4a Pro) |

Hand-drawn 25×25 and 13×13 glyphs are bundled for WhatsApp, Phone, Gmail, Telegram, Messages, Slack, and Instagram. Other apps fall back to a scaled version of their Android launcher icon.

## How it works

```
[Notifications] -> NotificationListenerService -> NotificationStore (in-memory)
                                                          |
[Priority settings (DataStore)] -----------------+        |
                                                 |        |
                                                 v        v
                                         GlyphSenseToyService
                                         (renders matrix on activation)
```

- A standard `NotificationListenerService` collects active notifications and writes minimal records (key, package, timestamp) to an in-memory store
- A DataStore-backed repository holds the user's per-app priority assignments, with sensible defaults seeded for ~18 common apps
- The toy is registered with Nothing's toy framework via the `com.nothing.glyph.TOY` action and standard meta-data
- When the user activates the toy, the service joins notifications with current priorities and renders frames with a 320ms fade transition between view changes
- Priority changes propagate live — flipping an app's setting updates the matrix the next time it refreshes

## Tech stack

- Kotlin + Coroutines / Flow
- Jetpack Compose + Material 3 (settings UI)
- DataStore Preferences (priority persistence)
- Nothing GlyphMatrix Developer Kit 2.0 (toy framework + matrix rendering)
- Min SDK 33, Target SDK 36

## Build

```bash
./gradlew assembleDebug
```

The Glyph Matrix SDK AAR ships in `app/libs/glyph-matrix-sdk-2.0.aar`. No external Maven coordinates required.

## Setup on device

1. Install the APK
2. Open GlyphSense and grant notification access in Settings
3. Adjust priorities in **Manage app priorities** (defaults cover most messaging apps)
4. Open Glyph Interface settings -> Glyph Toys -> enable **GlyphSense**
5. Press the Glyph Button to activate

## Project layout

```
app/src/main/java/com/glyphsense/app/
├── domain/   — Priority, PendingNotification, DeviceProfile
├── data/     — NotificationStore, PrioritySettingsRepository, InstalledAppsRepository
├── service/  — NotificationListenerService
├── glyph/    — GlyphSenseToyService, FrameRenderer, FrameTransitioner, AppGlyphs, ToyView
└── ui/       — Compose UI: home + app picker
```

## Editing the hand-drawn glyphs

Bitmaps live in `app/src/main/java/com/glyphsense/app/glyph/AppGlyphs.kt` as ASCII rows:

- `#` = pixel on (brightness 255)
- `+` `-` `:` = midtones (200, 130, 70)
- space or `.` = off

Edit any character in those string arrays to tune a glyph; no other file needs to change.

## Future improvements

- Persist pending notifications across process death
- Lazy icon loading in the app picker for users with very large app lists
- Hand-drawn glyphs for more apps (currently 7)
- Optional Always-On Display rendering (toy framework supports `EVENT_AOD`)

## Author

Mohit Sharma
