# Compose

We build our own copies of the compiler and runtime until certain changes are upstreamed into the
canonical copy in AOSP.

**Runtime differences:**

 * Enable multiplatform build
 * Add JVM runtime artifact target
 * Add JS runtime artifact target


# Updating

Ensure that your submodule is properly cloned. You can check by listing git remotes in the `upstream` directory. 

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

To update where the compose submodule HEAD points to, run: 

```
$ cd treehouse/compose/upstream
$ git fetch origin
$ git checkout origin/androidx-main
$ cd -
$ git add treehouse/compose/upstream 
```

Run a full `./gradlew -p treehouse clean build` and `./gradlew clean build`.

If any of the checks in `treehouse/compose/build.gradle` fail, look in
`upstream/gradle/libs.versions.toml` for the Kotlin and kotlinx.coroutines versions in use
and update `build.gradle` to match.
