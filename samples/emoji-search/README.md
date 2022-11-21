Emoji Search
============

This is a mobile app demo of Zipline. It has two modules:

 * **presenters** is a Kotlin/Multiplatform library that searches a set of emoji images.
 * **android** is an Android application that downloads the presenters JavaScript and displays it.
 * **ios** is an iOS application that downloads the presenters JavaScript and displays it.

Prerequisites
-------------

In order to build and run these applications you'll need to:
- Have Android Studio installed


Serving presenters.js
---------------------

Run this:

```
./gradlew :samples:emoji-search:presenters:serveDevelopmentZipline --info --continuous
```

This will compile Kotlin/JS and serve it at [[http://localhost:8080/presenters.js]]. The server will
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
cd samples/emoji-search/ios/app
pod install
open EmojiSearchApp.xcworkspace
```

Then build and run the app. The shared Kotlin code will be built automatically as part of building the iOS app, and also rebuilt as needed.

The app pulls the JavaScript from the presenters server and requires it to be running in order to work.


Live Edits
----------

Make changes to `RealEmojiSearchPresenter` - this will trigger a recompilation of the Zipline code.
When it's ready, relaunch the Android application.
