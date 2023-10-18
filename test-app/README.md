Test App
============

This is an app to aid in Redwood development. The app itself is a list of screens that can be
expanded upon to develop particular use cases or demonstrate bugs and performance problems.


Prerequisites
-------------

In order to build and run these applications you'll need to have the following installed:
- Android Studio
- gradle
- git-lfs


Serving the JS
--------------

Run this:

```
./gradlew :test-app:presenter-treehouse:serveDevelopmentZipline --info --continuous
```

This will compile Kotlin/JS and serve it at http://localhost:8080/presenter-treehouse.js. The server will
run until you CTRL+C the process.


Running Test App on Android
-------------------------------

Or this (to use the Android View frontend):

```
./gradlew :test-app:android-views:installDebug
```

This Android app assumes it's running in an emulator and will attempt to fetch JavaScript from the
devserver running on the host machine (10.0.2.2). It will crash if that server is not reachable (see above).


Running Test App on iOS
---------------------------

Run this:
```
open test-app/ios-uikit/TestApp.xcodeproj
```

Then build and run the app. The shared Kotlin code will be built automatically as part of building the iOS app, and also rebuilt as needed.
NB: To see local changes to the shared Kotlin code reflected in the iOS app, be sure to fully re-build the project (i.e. using ⌘R, not ⌃⌘R).

The app pulls the JavaScript from the presenters server and requires it to be running in order to work.


Running Test App on Web
---------------------------

To build and modify the app locally, run this:
```
./gradlew test-app:browser:browserRun
```

If successful, the command will load the app at http://localhost:8080/ in your default web browser.

Note: only one app instance can be loaded at a time, so if you already have another app instance open, this will clobber that.

Live Edits
----------

Make changes to files in `test-app/presenter/*` - this will trigger a recompilation of the Zipline code.

Add New Apps
---------------

Add new apps to the `screens` map at the top of `test-app/presenter/src/TestApp.kt` to display them on the main list.
