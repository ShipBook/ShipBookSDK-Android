# Shipbook Android examples

- `shop-app` — a small shop app with the Shipbook SDK, used in the "Android crash you can't reproduce" demo

The SDK's own sample app lives in [`app/`](../app) and builds against the SDK source; the examples here use the published `io.shipbook:shipbooksdk` artifact, the same way your app does.

## Crash demo: "the app crashes on one order"

The app lists three orders. Two are deliveries; #4812 is a store pickup, so it has no delivery address. The order screen handles pickup correctly, but **Track order** assumes every order has an address (`order.address!!`) and crashes on #4812. Test only with delivery orders and you'll never see it.

1. Create an app in [console.shipbook.io](https://console.shipbook.io) and put its credentials in `shop-app/local.properties`:
   ```properties
   sdk.dir=/path/to/Android/sdk
   SHIPBOOK_APP_ID=<app id>
   SHIPBOOK_APP_KEY=<app key>
   ```
2. Open `shop-app` in Android Studio (or run `./gradlew installDebug`) and start it on an emulator or device.
3. Open **Order #4812** and tap **Track order**. The app crashes.
4. Open the app again. The SDK uploads the crash when the app starts.
5. In the Console, open **Loglytics**: the `NullPointerException` at `OrderActivity.kt` is listed with its sessions. The session's logs show `fulfillment=PICKUP … address=null` right before the crash.
6. With GitHub connected and the app mapped to its repository ([Code Repositories](https://www.shipbook.io/docs/account_preferences/code-repositories)), **Analyze with AI** points to the line, and **Create pull request** opens the fix.
