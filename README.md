# ShipBook SDK for Android

[![Maven Central](https://img.shields.io/maven-central/v/io.shipbook/shipbooksdk.svg)](https://central.sonatype.com/artifact/io.shipbook/shipbooksdk)

Shipbook SDK for Android applications. Capture logs, errors, and exceptions from your Android apps and view them in the [Shipbook console](https://console.shipbook.io/). Learn more at [shipbook.io](https://www.shipbook.io/).

## Installation

Add to your `build.gradle`:

```groovy
dependencies {
    implementation 'io.shipbook:shipbooksdk:1.8.3'
}
```

## Quick Start

```kotlin
import io.shipbook.shipbooksdk.ShipBook
import io.shipbook.shipbooksdk.Log

// Initialize Shipbook (do this once in Application.onCreate)
ShipBook.start(application, "YOUR_APP_ID", "YOUR_APP_KEY")

// Get a logger for your class/component
val log = ShipBook.getLogger("MainActivity")

// Log messages at different severity levels
log.v("Detailed trace information")
log.d("Debug information")
log.i("General information")
log.w("Warning message")
log.e("Error message")

// Log with an exception
try {
    throw IllegalStateException("Something failed")
} catch (e: Exception) {
    log.e("Operation failed", e)
}
```

## Features

- **Remote Logging** - View all your app logs in the Shipbook console
- **Error Tracking** - Automatically captures uncaught exceptions
- **Session Tracking** - Group logs by user session
- **Offline Support** - Logs are queued and sent when connectivity is restored
- **Dynamic Configuration** - Change log levels remotely without redeploying
- **User Identification** - Associate logs with specific users

## Configuration

### Enable Inner Logging (Debug Mode)

```kotlin
ShipBook.enableInnerLog(true)
```

### Register User

```kotlin
ShipBook.registerUser(
    "user-123",
    userName = "johndoe",
    fullName = "John Doe",
    email = "john@example.com",
    phoneNumber = "+1234567890",
    additionalInfo = mapOf("role" to "admin")
)
```

### Logout

```kotlin
ShipBook.logout()
```

### Screen Tracking

```kotlin
ShipBook.screen("HomePage")
```

### Static Log Methods

You can also use static methods without creating a logger instance:

```kotlin
Log.e("MyTag", "Something went wrong")
Log.w("MyTag", "This is a warning")
Log.i("MyTag", "General info")
Log.d("MyTag", "Debug info")
Log.v("MyTag", "Trace info")
```

## Getting Your App ID and Key

1. Sign up at [shipbook.io](https://www.shipbook.io/)
2. Create a new application in the console
3. Copy your App ID and App Key from the application settings

For full setup instructions, see the [Android documentation](https://docs.shipbook.io/android-log-integration).

## Links

- [Shipbook Website](https://www.shipbook.io/)
- [Shipbook Console](https://console.shipbook.io/)
- [Documentation](https://docs.shipbook.io/)
- [GitHub Repository](https://github.com/ShipBook/ShipBookSDK-Android)

## Author

Elisha Sterngold ([ShipBook Ltd.](https://www.shipbook.io))

## License

ShipBook SDK is available under the MIT license. See the [LICENSE](LICENSE) file for more info.
