# Changelog

## 2.0.0
- Upgrade to get an SDK that follows the Shipbook server: it stops sending when the server refuses your app or is busy, and calling `ShipBook.start` again costs nothing.
- The SDK obeys the server. If the server refuses the app (an unknown app ID, or traffic far past fair use), the SDK pauses: it sends nothing and drops new logs until the pause ends, at most 30 days and across restarts. If the server or a proxy answers busy (429 or 503), it waits (the `Retry-After` time, 5 minutes without one, at most an hour) and sends the saved logs afterwards. `ShipBook.enableInnerLog(true)` prints `paused until …` or `backing off until …`.
- Every request carries `x-shipbook-app-id` and `x-shipbook-sdk: android/2.0.0`.
- Calling `ShipBook.start` again with the same app ID and key keeps the current session. A completion passed then replaces the earlier one, and is called at once if login already finished.
- Fixed: a crash in an Activity, Service or receiver lifecycle method (`onCreate`, `onResume`, …) is now reported as your own exception, with your file and line. Before, it showed as Android's `Unable to start activity` wrapper, and all such crashes fell into one Loglytics issue.
- Fixed: a crash could be lost when the app died before it was saved. It is now written to disk before the crash handler returns.
- Removed the `uri` parameter of `ShipBook.start`, which read a config file nobody passed.

## 1.10.0
- Added `ShipBook.getSessionHeaders()`. Add its headers to requests toward your own backend, and the Shipbook console links this app session to the sessions those requests create in your Node.js backend. It returns an empty map until login completes, so adding it is always safe.

## 1.9.2
- Fixed a case where the SDK never logged in again after its cached `config.json` was left corrupt by a crash or power loss mid-write. The corrupt file blocked login on every later start until app data was cleared. The SDK now discards an unreadable cached config, falls back to the default config, and logs in normally.
- The cached config is now written atomically (temp file + fsync + rename) so a kill mid-write can no longer leave a partially written file behind.

## 1.9.1
- Stop retrying `loginSdk` after a 4xx response — broken credentials (bad appId/appKey, app deleted) no longer cause a per-timer-tick loop against the server until the next app start.
- Refresh path also sets the no-retry flag on a 4xx refresh response so it doesn't fall through to a fresh `loginSdk` that would 4xx too.

## 1.9.0
- Replaced deprecated `LocalBroadcastManager` with Kotlin `SharedFlow` for internal event handling.
- Removed `androidx.localbroadcastmanager` dependency.
- Eliminated Timer-based retry hack in `Log` by using `SharedFlow` with replay.
- Updated dependencies: Kotlin 1.9.22, Coroutines 1.6.0, Gradle 8.13, AGP 8.13.2, AppCompat 1.4.1.
- Added custom appender support.
- Optimized file loading.
- Clean shutdown of appenders.
- Build config information sourced from BuildConfig.
- Fixed all Kotlin compiler warnings.

## 1.8.3
- Fixed issue when getting to log limit.

## 1.8.2
- Fixing for different return codes.

## 1.8.1
- Fixing cases of not connecting to server.

## 1.8.0
- Making SBCloudAppender work with obfuscation.
- Upgrade to the latest version of Kotlin.
- Upgrade Android Studio version to Iguana.