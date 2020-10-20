# ShipBook SDK for Android

## About ShipBook

[ShipBook](https://www.shipbook.io) gives you the power to remotely gather, search and analyze your user logs and exceptions in the cloud, on a per-user & session basis.

The SDK is written in Kotlin, but works perfectly well in Java. The following examples in this documentation are written in Java.

---
## Requirements
ShipBook works with min SDK version 19 (KITKAT)

---
## Installation

ShipBookSDK is available through jcentral. 

To install it, simply add the following line to the dependencies in your build.gradle: `implementation 'io.shipbook.shipbooksdk:shipbooksdk:1.+'`

---
## Integrating Shipbook into your code
Add the following to your application file:

```java
import io.shipbook.shipbooksdk.ShipBook;
```

And add the following to `onCreate()`:

```java
ShipBook.start(this,"YOUR_APP_ID", "YOUR_APP_KEY");
```

### Quick Implementation
You can call all the usual Android logs, the only difference is that you should change the import from `import android.util.Log;` to `import io.shipbook.shipbooksdk.Log;`.
for example:
```java
import io.shipbook.shipbooksdk.Log;

...
Log.e(TAG, "the log message"); // Error log
Log.w(TAG, "the log message"); // Warning log
Log.i(TAG, "the log message"); // Info log
Log.d(TAG, "the log message"); // Debug log
Log.v(TAG, "the log message"); // Verbose log
```

### Simpler Implementation
ShipBook employs a simpler system for logs because the static logger causes the following issues:

* Implementation is slower, especially in cases where the log is closed.
* You need to add the word TAG for each log.

To have a log on each class you will need to create a logger:
```java
import io.shipbook.shipbooksdk.ShipBook;
...

// in the class
static Log log = ShipBook.getLogger("TAG");
```
The TAG should be named for the specific tag of your choice. The convention is to use the class name.

Usage of the log:
```java
log.e("the log message"); // Error log
log.w("the log message"); // Warning log
log.i("the log message"); // Info log
log.d("the log message"); // Debug log
log.v("the log message"); // Verbose log
```

---
## Enable Shipbook debug logging
If your logs weren't uploaded to Shipbook, or you're experiencing some other issue with Shipbook, you can enable Shipbook debug logging to track down the problem.

```java
ShipBook.enableInnerLog(true);
```

---
## Linking Shipbook to a User’s Information
The SDK allows the option to associate each session with specific user information.

### Register user:
The best practice is to set registerUser before ShipBook.start. It will also work after this point however, it will require an additional api request.

```java
ShipBook.registerUser("USER_ID", "USER_NAME", "FULL_NAME", "USER_EMAIL", "USER_PHONE_NUMBER", "additional info");
```
The only parameter that must be entered is the `userId`. You may set all the other parameters to `null`.


### Logout
To logout the user, add the following code to your app’s logout function.
```java
ShipBook.logout();
```

### Screen

To log the user’s screen information, add the following code
```java
ShipBook.screen("SCREEN_NAME")
```

## Using Wrappers with ShipBook
If you are already using some kind of a logging system, you may want to write wrappers to send the logs to both systems.

You will need to add the wrapper class name to addWrapperClass 
```java
ShipBook.addWrapperClass(LogWrapper.class.getName())
```

## Integrating Shipbook with Crashlytics
To integrate Shipbook with crashlytics just implement the completion callback of `ShipBook.start` as following:

```java
ShipBook.start(this,"YOUR_APP_ID", "YOUR_APP_KEY", (sessionUrl)-> {
            Crashlytics.setString("shipbookSession", sessionUrl);
            return Unit.INSTANCE;
        });
```

(in Kotlin you don't need `return Unit.INSTANCE`)

Now you'll have in crashlytics a link to the logs of the specific session. The link is in the Keys tab of the a specific crash under the key `ShipbookSession`.

---

## Integrating with [Timber](https://github.com/JakeWharton/timber)

Just add the following code and it will work out of the box with timber.

```java
  ShipBook.addWrapperClass(Timber.class.getName());
  Timber.plant(new Timber.Tree() {
      {
          ShipBook.addWrapperClass(this.getClass().getName());
      }

      @Override
      protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
          Log.message(tag, message, priority, t);
      }
  });
```

## Obfuscation with Shipbook
In the case that the build is obfuscated to let all the functionality in Shipbook to work add the following lines to your Proguard 
```proguard
   -keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
```

---

## Author

Elisha Sterngold ([ShipBook Ltd.](https://www.shipbook.io))

## License

ShipBookSDK is available under the MIT license. See the LICENSE file for more info.
