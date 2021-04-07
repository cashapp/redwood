# Compose

We build our own copies of the compiler and runtime until certain changes are upstreamed into the
canonical copy in AOSP.

**Runtime differences:**

 * Enable multiplatform build
 * Add JVM runtime artifact target
 * Add JS runtime artifact target

**Compiler differences:**

 * Support JS_IR backend output


# Updating

Note: Currently we are targeting the [JetBrains/androidx](https://github.com/JetBrains/androidx)
repository instead of AOSP. This fork is farther along in its JS and Native support for the compiler
plugin.

```
$ cd compose/upstream
$ git fetch origin
$ git checkout origin/compose-web-main
$ cd -
$ git add compose/upstream 
```

Run a full `./gradlew clean build`.

If any of the checks in `compose/build.gradle` fail, look in
`upstream/buildSrc/build_dependencies.gradle` for the Kotlin and kotlinx.coroutines versions in use
and update `build.gradle` to match.
