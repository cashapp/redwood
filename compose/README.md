# Redwood's Compose

We build our own copy of the Compose runtime as a multiplatform project with JVM, JS, and native
targets in addition to the regular Android. All sources come from
[JetBrains' fork](https://github.com/JetBrains/androidx/) which are included as a git submodule.

The compiler and "hosted" compiler (i.e., the one which works on Kotlin/Native targets) are also
from JetBrains, but we use their binary artifacts from Maven Central.


## Updating

_First time? Check the [prerequisites](#Prerequisites)._

Despite building our own Compose we only build released versions (to enable dependency substitution).

We depend on both Google's Compose artifacts and JetBrains' Compose artifacts and need versions for both.
While stable builds will align, pre-release builds do not.

Update the `gradle/libs.versions.toml` file to reference the versions you want.
```diff
-androidCompose = "1.2.0-alpha08"
-jbCompose = "1.2.0-alpha01-dev641"
+androidCompose = "1.2.0"
+jbCompose = "1.2.0-alpha01-dev741"
```

JetBrains does not publish SHAs for released versions and the git repo does not have tags.
For stable versions, there will be a branch named `release/X.Y.Z`.
For unstable versions, find the associated tag in the [compose-jb](https://github.com/JetBrains/compose-jb) repo and then grab the SHA of the `compose/frameworks/support` git submodule.

Update the Compose submodule to the desired SHA:

```
$ cd compose/upstream
$ git fetch origin
$ git checkout <REF>
$ cd -
```

Replacing `<REF>` with `release/X.Y.Z` or `jb-main` or whatever.

Run a full `./gradlew -p redwood clean build` and `./gradlew clean build`.

If any of the checks in `compose/build.gradle` fail, look in
`compose/upstream/gradle/libs.versions.toml` for the Kotlin and kotlinx.coroutines versions in use
and update `gradle/dependencies.gradle` to match.

Once everything builds, you're done. Commit, push, and PR!


### Prerequisites

You only have to do this once!

Ensure that your submodule is properly cloned.
You can check by listing git remotes in the `upstream` directory. 

```
$ cd compose/upstream
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
