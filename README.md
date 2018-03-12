# ShipBook SDK for Android

ShipBook is a cloud service to collect mobile application logs. It also logs UI events and crashes. By having all this information together it is possible to analyze problems easily.

All logs go by default also to the logcat.

The SDK has very special emphasis on being very fast in the case that the log is closed. Therefore feel free to put as many logs as needed, it won't impact the performance of the app (when logs are closed)

The SDK is written in Kotlin, but works perfectly in Java as well. All the examples that I brought are in Java.

---
## Requirements
ShipBook works with min sdk version 19 (KITKAT)

---
## Installation

ShipBookSDK is available through jcentral. To install
it, simply add the following line to your build.gradle in the dependencies: `implementation 'io.shipbook.shipbooksdk:shipbooksdk:0.1.0'`

---
##  Using ShipBook
In your application file:

```java
import io.shipbook.shipbooksdk.ShipBook;
```

And add the following to `onCreate()`:

```java
ShipBook.start(this,"YOUR_APP_ID", "YOUR_APP_KEY");
```

### The simplest implementation
You can call all the usual android logs, the only difference is that you should change the import from `import android.util.Log;` to `import io.shipbook.shipbooksdk.Log;`.
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

### Calling log with `getLogger`
Working with the static logger isn't ideal:
* The implementation is much slower especially in cases that the log is closed.
* You need each time to add the word TAG for each log.

Therefore we created a simpler system for logs. 

For to have a log on each class you should create a logger:
```java
import io.shipbook.shipbooksdk.ShipBook;
...

// in the class
static Log log = ShipBook.getLogger("TAG");
```
The tag should be the specific tag of your choice. The convention is to use the class name for it.

The usage of the log:

```java
log.e("the log message"); // Error log
log.w("the log message"); // Warning log
log.i("the log message"); // Info log
log.d("the log message"); // Debug log
log.v("the log message"); // Verbose log
```

---

## User associated data
The SDK enables the option to associate the session with specific user information

### Register user:
```java
ShipBook.registerUser("USER_ID", "USER_NAME", "FULL_NAME", "USER_EMAIL", "USER_PHONE_NUMBER", "additional info");
```
The only parameter that must be entered is the `userId`. For all the other parameters you can set null

The best position to set the registerUser is before the `ShipBook.start`. It will also work after it, but there will need to be one more api request.

### Logout
When a user logs out of the system then a new session is created.
```java
ShipBook.logout();
```

---


## Author

Elisha Sterngold (ShipBook Ltd.)

## License

ShipBookSDK is available under the MIT license. See the LICENSE file for more info.
