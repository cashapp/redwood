# Change Log

## [Unreleased]


## [0.3.0] - 2023-05-15

New:

- Support for testing Composables with new test-specific code generation. Use the
  'app.cash.redwood.generator.testing' plugin to generate a lambda-accepting entrypoint function
  (such as `ExampleTester()`). Inside the lambda you can await snapshots of the values which
  would be bound to the UI widgets at that time.
- Redwood Layout now contains a `Spacer` which can be used to create negative space separately
  from padding (which otherwise disappears when the item disappears).
- The host's safe area insets are now included in `HostConfiguration`. Note that these are global
  values which should only be applied when a view is known to be occupying the full window size.
- Use the host's native frame rate to trigger recomposition inside of Treehouse. Pending snapshot
  changes are also required for recomposition to occur.

Changes:

- Widgets are now created, populated, and attached to the native view hierarchy in a different order
  than before. Previously widget was created, attached to its parent, and then its properties were
  all set followed by any language modifiers. Now, the widget is created, all of its properties and
  layout modifiers are set, and then it is added to its parent. Additionally, widgets are added to
  their parents in a bottom-up manner. Code like `Row { Column { Text } }` will see `Text` be added
  to `Column` before `Column` is added to `Row.
- 'redwood-treehouse' module has been split into '-shared', '-guest', and '-host' modules to
  more cleanly delineate where each is used. "Host" is the native application and "guess" is code
  running inside the Zipline JS VM.
- Schema dependencies are not longer parsed when loading a schema. Instead, a JSON representation
  is loaded from the classpath which contains the parsed structure of the dependency. As a result,
  the module which contains the schema files must apply the 'app.cash.redwood.schema' plugin in
  order to create this JSON.
- Redwood Layout's `Padding` type is now called `Margin`.
- Both Redwood's own API as well as code generated from your schema is now annotated with
  `@ObjCName` to create better-looking APIs in Objective-C (and Swift).
- The `@Deprecated` annotation on a widget or its properties will now propagate into the generated
  Composable and widget interface.
- Event types are no longer always nullable. They will now respect the nullability in the schema.
- Layout modifiers are now generated into a 'modifier' subpackage.

Fixed:

- Redwood Layout `Constraint`s are now correctly propagated into HTML.

This version only works with Kotlin 1.8.20.


## [0.2.1] - 2023-01-31

Changed:
- Do not use a `ScrollView`/`HorizontalScrollView` as the parent container for View-based `Row` and
  `Column` display when the container is not scrollable (the default). Use a `FrameLayout` instead.

Fixed:
- Actually publish the `redwood-treehouse-composeui` artifact.

This version only works with Kotlin 1.7.20.


## [0.2.0] - 2023-01-30

New:
- `redwood-layout-dom` module provides HTML implementations of `Row` and `Column`.
- Lazy layout's schema artifacts are now published and can be used by other projects.
- Expose `concurrentDownloads` parameter for `TreehouseApp.Factory`. The default is 8.
- Add `moduleLoadStart` and `moduleLoadEnd` events to Treehouse's `EventListener`.

Changed:
- Compile with Android API 33.
- Counter sample now uses shared `Row` and `Column` layouts rather than its own unspecified one.
- JSON serialization on the Compose-side of Treehouse is now faster and emits dramatically
  less code than before.
- Create a dedicated `CoroutineScope` for each `TreehouseView`. When a view leaves, its coroutines
  can now be immediately canceled without waiting for anything on the application-side.
- `TreehouseLauncher` is now called `TreehouseApp.Factory`. Additionally, when you `create()` a
  `TreehouseApp` from a factory you must also call `start()` for it to actually start.
- Use platform-specific collections types in JS for the Compose-side of Treehouse. This is faster,
  more memory-efficient, and produces less code.
- Update to Zipline 0.9.15.

Fixed:
- Do not expose Gradle `Configuration`s created by our plugin. This ensures they are not candidates
  for downstream modules to match against when declaring a dependency on a project using the plugin.
- Change when the Treehouse `FrameClock` is closed to avoid crashing on updates.

This version only works with Kotlin 1.7.20.


## [0.1.0] - 2022-12-23

Initial release.

This version only works with Kotlin 1.7.20.



[Unreleased]: https://github.com/cashapp/redwood/compare/0.3.0...HEAD
[0.3.0]: https://github.com/cashapp/redwood/releases/tag/0.3.0
[0.2.1]: https://github.com/cashapp/redwood/releases/tag/0.2.1
[0.2.0]: https://github.com/cashapp/redwood/releases/tag/0.2.0
[0.1.0]: https://github.com/cashapp/redwood/releases/tag/0.1.0
