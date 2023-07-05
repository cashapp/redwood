# Releasing

1. Update `redwoodVersion` in `RedwoodBuildPlugin.kt` to the release version.

2. Update the `CHANGELOG.md`:
   1. Change the `Unreleased` header to the release version.
   2. Add a link URL to ensure the header link works.
   3. Add a new `Unreleased` section to the top.

3. Update the `README.md`:
   1. Update the Kotlin compatibility table with the new version.
   <!--2. Update the "Usage" section to reflect the new release version and the snapshot section to reflect the next "SNAPSHOT" version.-->

4. Commit

   ```
   $ git commit -am "Prepare version X.Y.Z"
   ```

5. Tag

   ```
   $ git tag -am "Version X.Y.Z" X.Y.Z
   ```

6. Update `redwoodVersion` in `RedwoodBuildPlugin.kt` to the next "SNAPSHOT" version.

7. Commit

   ```
   $ git commit -am "Prepare next development version"
   ```

8. Push!

   ```
   $ git push && git push --tags
   ```

   The tag will trigger a GitHub Action workflow which will upload the artifacts to Maven Central,
   create a GitHub release, and deploy the documentation to the website.
