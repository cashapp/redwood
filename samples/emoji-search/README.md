Emoji Search
============

This is a mobile app demo of Zipline.

 * **presenter** is a Kotlin/JS @Composable which searches a set of emoji images.
 * **presenter-treehouse** is a Kotlin/Multiplatform library which exposes the presenter to a host application.
 * **android** is an Android application that downloads the presenter JavaScript and displays it.
 * **ios** is an iOS application that downloads the presenter JavaScript and displays it.
 * **browser** is a web application that downloads the presenter JavaScript and displays it.


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
./gradlew :samples:emoji-search:presenter-treehouse:serveDevelopmentZipline --info --continuous
```

This will compile Kotlin/JS and serve it at http://localhost:8080/presenter-treehouse.js. The server will
run until you CTRL+C the process.


Running Emoji-Search on Android
-------------------------------

Run this (to use the Compose UI frontend):

```
./gradlew :samples:emoji-search:android-composeui:installDebug
```

Or this (to use the Android View frontend):

```
./gradlew :samples:emoji-search:android-views:installDebug
```

This Android app assumes it's running in an emulator and will attempt to fetch JavaScript from the
devserver running on the host machine (10.0.2.2). It will crash if that server is not reachable (see above).


Running Emoji-Search on iOS
---------------------------

Run this:
```
cd samples/emoji-search/ios-uikit
pod install
open EmojiSearchApp.xcworkspace
```

Then build and run the app. The shared Kotlin code will be built automatically as part of building the iOS app, and also rebuilt as needed.
NB: To see local changes to the shared Kotlin code reflected in the iOS app, be sure to fully re-build the project (i.e. using ⌘R, not ⌃⌘R).

The app pulls the JavaScript from the presenters server and requires it to be running in order to work.


Running Emoji-Search on Web
---------------------------

To simply view the sample app, open: https://cashapp.github.io/redwood/latest/emoji-search/

To build and modify the app locally, run this:
```
./gradlew samples:emoji-search:browser:browserRun
```

If successful, the command will load the app at http://localhost:8080/ in your default web browser.

Note: only one app instance can be loaded at a time, so if you already have another app instance open, this will clobber that.


Live Edits
----------

Make changes to `RealEmojiSearchPresenter` - this will trigger a recompilation of the Zipline code.
When it's ready, relaunch the Android application.
