Counter
=======

This is a very basic demo of Redwood.


Prerequisites
-------------

In order to build and run these applications you'll need to have the following installed:
- gradle
- git-lfs


Running Counter on Android
----------------------

Run this (to use the Compose UI frontend):

```
./gradlew :samples:counter:android-composeui:installDebug
```

Or this (to use the Android View frontend):

```
./gradlew :samples:counter:android-views:installDebug
```


Running Counter on Desktop
----------------------

Run this:

```
./gradlew :samples:counter:desktop-composeui:run
```


Running Counter on iOS
----------------------

Run this:
```
open samples/counter/ios-uikit/CounterApp.xcodeproj
```

Then build and run the app. The shared Kotlin code will be built automatically as part of building the iOS app, and also rebuilt as needed.


Running Counter on Web
----------------------

To simply view the sample app, open: https://cashapp.github.io/redwood/latest/counter/

To build and modify the app locally, run this:
```
./gradlew samples:counter:browser:jsBrowserRun
```

If successful, the command will load the app at http://localhost:8080/ in your default web browser.

Note: only one app instance can be loaded at a time, so if you already have another app instance open, this will clobber that.
