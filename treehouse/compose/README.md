# Compose

We build our own copies of the compiler and runtime until certain changes are upstreamed into the
canonical copy in AOSP.

**Runtime differences:**

 * Enable multiplatform build
 * Add JVM runtime artifact target
 * Add JS runtime artifact target


# Updating

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
