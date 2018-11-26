# qs8relay_android
Android Apk for management a QS-8Relay module.


PRE) You need to pair with device before use the application. Use bluetooth on android system to do it.

App features:

1) Include background service for auto-update relays module each 30 seconds. The service start at mobile boot but is necessary that doesn't close main app.

2) When device is selected in the next view, can be configured relays name and device name.

3) State saved will be passed to background service to auto-update.

4) If app is closed you can run background service by re-open the application and send to background.

5) When communicate with device you can configure number of tries and milliseconds between each try. You can configure it on device config view.

6) The app has a widget for see and update background service.

Thanks you for see my repo!

