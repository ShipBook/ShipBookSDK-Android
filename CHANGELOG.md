# Changelog

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