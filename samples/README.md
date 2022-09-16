Redwood Samples
===============

This project requires locally publishing the enclosing redwood project before its own Gradle build
can be launched. These artifacts must also be re-published each time the enclosing project changes.

```
./gradlew publishAllPublicationsToLocalMavenRepository -PVERSION_NAME=1.0-local
```
