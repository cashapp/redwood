# Treehouse's Compose

We build our own copy of the Compose runtime as a multiplatform project with JVM, JS, and native
targets in addition to the regular Android. The Android and JVM `actual`s are sourced from AOSP,
but those for JS and native come from [JetBrains' fork](https://github.com/JetBrains/androidx/)
and are copied directly into the project.

We build our own copy of the compiler (unshaded) for use with native.


## Updating

_First time? Check the [prerequisites](#Prerequisites)._

Despite building our own Compose we only build released versions (to enable dependency substitution).

First, find the version you want on the [Compose Runtime releases](https://developer.android.com/jetpack/androidx/releases/compose-runtime) page and scroll to its section.

Update the `gradle/dependencies.gradle` file to reference this version.
```diff
 buildscript {
   ext.versions = [
-    'compose': '1.0.0-rc01',
+    'compose': '1.0.0-rc02',
```

Back on the Android documentation, click the "Version X.Y.Z contains these commits" link.
The URL will look roughly like https://android.googlesource.com/platform/frameworks/support/+log/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA..BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB/compose/runtime with the string of "A"s and "B"s replaced with a real git SHAs.
Copy the git SHA which corresponds to the "B"s.

Update the Compose submodule HEAD to this git SHA, run:

```
$ cd treehouse/compose/upstream
$ git fetch origin
$ git checkout BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
$ cd -
```

(replacing the "B"s with the real git SHA, obviously)

Run a full `./gradlew -p treehouse clean build` and `./gradlew clean build`.

If any of the checks in `treehouse/compose/build.gradle` fail, look in
`treehouse/compose/upstream/gradle/libs.versions.toml` for the Kotlin and kotlinx.coroutines versions in use
and update `gradle/dependencies.gradle` to match.

If either the `jsMain/` or `nativeMain/` sources fail to compile, the `actual`s may need updated.
The JS actuals come from the `compose-web-main` branch [here](https://github.com/JetBrains/androidx/tree/compose-web-main/compose/runtime/runtime/src/jsMain/).
The native actuals come from the `compose-native-main` branch [here](https://github.com/JetBrains/androidx/tree/compose-native-main/compose/runtime/runtime/src/nativeMain/).
Assuming JetBrains has updated their fork (which you may have to wait for), copy the new files into the Treehouse project at `treehouse/compose/runtime/src/jsMain/` and/or `treehouse/compose/runtime/src/nativeMain/`.

If any of the other modules fail to compile there are probably changes and/or deprecations in Compose that will need to be corrected.
This will be a problem for _any_ Compose-based project, so the official change log should have instructions.

Once everything builds, you're done. Commit, push, and PR!


### Prerequisites

You only have to do this once!

Ensure that your submodule is properly cloned.
You can check by listing git remotes in the `upstream` directory. 

```
$ cd treehouse/compose/upstream
$ git remote -v 
origin	https://android.googlesource.com/platform/frameworks/support (fetch)
origin	https://android.googlesource.com/platform/frameworks/support (push)
```

If the output of `git remote -v` lists anything other than the above remotes, then make sure you've cloned the submodule correctly.
This can be done by either recursively cloning the repo from scratch

```
$ git clone --recurse-submodules
```

Or by initializing the submodule in an existing copy of the repo. 

```
$ git submodule update --init
```
