# Change Log

## [Unreleased]
[Unreleased]: https://github.com/cashapp/redwood/compare/0.9.0...HEAD

New:
-

Changed:
- Disable klib signature clash checks for JS compilations. These occasionally occur as a result of Compose compiler behavior, and are safe to disable (the first-party JetBrains Compose Gradle plugin also disables them).

Fixed:
- Fix the `View` implementation of `Box` to wrap its width and height by default. This matches the behavior of the `UIView` implementation and all other layout widgets.
- Fix the `UIView` implementation of `Box` not updating when some of its parameters are changed.
- Fix `Modifier.size` not being applied to children inside a `Box`.
- Fix `Margin` not being applied to the `UIView` implementation of `Box`.


## [0.9.0] - 2024-02-28
[0.9.0]: https://github.com/cashapp/redwood/releases/tag/0.9.0

Changed:
- Added `Modifier` parameter to `RedwoodContent` which is applied to the root `Box` into which content is rendered (https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#elements-accept-and-respect-a-modifier-parameter).
- The parameter order of `LazyRow` and `LazyColumn` have changed to reflect Compose best practices (https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#elements-accept-and-respect-a-modifier-parameter).
- The parameter order of `TreehouseContent` has changed to reflect Compose best practices (https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#elements-accept-and-respect-a-modifier-parameter).
- The render function of `ComposeWidgetChildren` has been renamed to `Render` to reflect Compose best practices (https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md#naming-unit-composable-functions-as-entities).
- Disable decoy generation for JS target to make compatible with JetBrains Compose 1.6. This is an ABI-breaking change, so all Compose-based libraries targeting JS will also need to have been recompiled.

Fixed:
- Don't block touch events to non-subviews below a `Row`, `Column`, or `Box` in the iOS `UIView` implementation. This matches the behavior of the Android View and Compose UI implementations.

This version works with Kotlin 1.9.22 by default.


## [0.8.0] - 2024-02-22
[0.8.0]: https://github.com/cashapp/redwood/releases/tag/0.8.0

New:
- `flex(double)` modifier for layouts which acts as a weight along the main axis.
- Allow reserving widget, modifier, property, and children tags in the schema. This can be used to document old items which no longer exist and prevent their values from accidentally being reused.
- Add `dangerZone { }` DSL to the `redwood { }` Gradle extension which allows enabling Compose reports and metrics. Currently these features break build caching as Compose forces the use of absolute paths in the Kotlin compiler arguments when in use (hence why they're marked as dangerous).
- `BackHandler` composable provides a callback for handling hardware back affordances (currently only on Android).
- Expose `frameClock` on `StandardAppLifecycle` to allow monitoring host frames.
- `CodeListener.onUncaughtException` notifies of any uncaught exceptions which occur in Treehouse guest code.
- Preview: Add `Box` widget which stacks children on top of each other. This is currently only implemented for Android views and iOS UIKit.
- Support `rememberSaveable` in plain Redwood compositions.
- Programmatic scrolls on `LazyListState` can now set `animated=true` for an animated scroll.
- Add `ziplineCreated`, `manifestReady`, and `codeLoadSkippedNotFresh` event callbacks to Treehouse `EventListener`.

Changed:
- The Treehouse Zipline disk cache directory is no longer within the cache directory on Android. This ensures it can't be cleared while the app is running. Zipline automatically constrains the directory to a maximum size so old entires will still be purged automatically.
- Set the Zipline thread's stack size to 8MiB on Android to match iOS.
- Use `margin-inline-start` and `margin-inline-end` for the start and end margin, respectively, for the HTML DOM layout bindings.
- `TestRedwoodComposition` now accepts only the initial `UiConfiuration` and exposes a `MutableStateFlow` for changing its value over time.
- `TreehouseLayout` now defines a default ID to allow state saving and restoration to work. Note that this will only work when a single instance is present in the hierarchy. If you have multiple, supply your own unique IDs.
- Emoji Search sample applications now bundle the latest guest code at compile-time and do not require the server running to work.
- The built-in `RedwoodView` for `HTMLElement` now reports density changes to the `UiConfiguration`.
- Redwood protocol modules have been renamed to 'guest' and 'host' to match Treehouse conventions.
- Suppress deprecation warnings in generated code. This code often refers to user types which may be deprecated, and should not cause additional warnings.
- `TreehouseAppContent.preload` is now idempotent.
- `LazyList` on iOS has changed from `UICollectionView` to `UITableView`, and changes to the backing data are now reported granularly rather than reloading everything.
- Allow arbitrary serializable content within `rememberSaveable` inside Treehouse.
- Add a `TreehouseApp` argument to `CodeListener`. Combined with the new uncaught exception callback, this provides an easy way to restart a Treehouse application on a crash.
- `EventListener.Factory` instances are now supplied as part of a `TreehouseApp` instead of a `TreehouseAppFactory`. This more closely scopes them with the lifetime of the `Zipline` instance.

Fixed:
- Ensure changes to modifiers notify their parent widget when using Treehouse.
- Explicitly mark the generated scope objects as `@Stable` to prevent needless recomposition.
- Dispose the old composition when the `RedwoodContent` composable recomposes or is removed from the composition.
- Ensure `UIViewChildren` indexes children using `typedArrangedSubviews` when removing views from a `UIStackView`.
- Correctly parse `data object` modifiers in the schema.
- Remember the default `CodeListener` for `TreehouseContent` to avoid unneccessary recomposition on creation.
- When calling `TreehouseUi.start`, fall back to older API signature when newer one does not match. This is needed because an addiitonal parameter was added in newer versions, but older guest code may have the old signature.
- Persist saved values from Treehouse without jumping back to the UI thread which allows proper restoration after a config change.
- Reset the requested widths and heights of a layout in the underlying Yoga engine when the size is invalidated. This ensures that the engine will properly measure changed content the grows and shrinks in either dimension.

This version works with Kotlin 1.9.22 by default.


## [0.7.0] - 2023-09-13
[0.7.0]: https://github.com/cashapp/redwood/releases/tag/0.7.0

New:
- Expose viewport size and density in `UiConfiguration`.
- `RedwoodView` and platform-specific subtypes provide a turnkey view into which a
  `RedwoodComposition` can be rendered. `TreehouseView` now extends `RedwoodView`.

Changed:
- Remove support for the Kotlin/JS plugin (`org.jetbrains.kotlin.js`). This plugin is deprecated
  and projects should be migrated to Kotlin multiplatform plugin (`org.jetbrains.kotlin.multiplatform`).
- Some `TreehouseView` subtypes were renamed to better match platform conventions:
  - `TreehouseWidgetView` is now `TreehouseLayout` for Android.
  - `TreehouseUIKitView` is now `TreehouseUIView` for iOS.
- `UIViewChildren` now supports `UIStackView` automatically.
- Package name of types in 'lazylayout-dom' artifact is now `lazylayout` instead of just `layout`.

This version works with Kotlin 1.9.10 by default.


## [0.6.0] - 2023-08-10
[0.6.0]: https://github.com/cashapp/redwood/releases/tag/0.6.0

New:
- Support for specifying custom Compose compiler versions. This will allow you to use the latest
  version of Redwood with newer versions of Kotlin than it explicitly supports.

  See [the README](https://github.com/cashapp/redwood/#custom-compose-compiler) for more information.
- `LazyList` can now be programmatically scrolled through its `ScrollItemIndex` parameter.
- Pull-to-refresh indicator color on `LazyList` is now customizable through
  `pullRefreshContentColor` parameter.

Changes:
- Many public types have been migrated away from `data class` to regular classes with
  `equals`/`hashCode`/`toString()`. If you were relying on destructuring or `copy()` for these
  types you will need to migrate to doing this manually.

Fix:
- The emoji search browser sample no longer crashes on first load.
- Lots of rendering and performance fixes for UIKit version of `LazyList`
  - Only measure items which are visible in the active viewport.
  - Remove some default item spacing imposed by the backing `UICollectionViewFlowLayout`.
  - Share most of the internal bookkeeping logic with the Android implementations for consistency
    and correctness.
  - Placeholders are now correctly sized along the main axis.

This version works with Kotlin 1.9.0 by default.


## [0.5.0] - 2023-07-05
[0.5.0]: https://github.com/cashapp/redwood/releases/tag/0.5.0

This release marks Redwood's "beta" period which provides slightly more stability guarantees than
before. All future releases up to (but NOT including) 1.0 will have protocol and service
compatibility with older versions. In practice, what this means is that you can use Redwood 0.6
(and beyond) to compile and deploy Treehouse guest code which will run inside a Treehouse host
from Redwood 0.5.

Redwood still reserves the right to make binary- and source-incompatible changes within the host
code or within the guest code.

New:
- The relevant tags and names from your schema will now automatically be tracked in an API file and
  changes will be validated to be backwards-compatible. The `redwoodApiGenerate` Gradle task will
  generate or update the file, and the `redwoodApiCheck` task will validate the current schema as
  part of the `check` lifecycle task.
- `width`, `height`, and `size` modifiers allow precise control over widget size within
  Redwood layout.
- Preliminary support for `rememberSaveable` within Treehouse guest code with persistence only
  available on Android hosts.

Changes:
- The flexbox implementation has changed from being a Kotlin port of the Google's Java flexbox
  layout to using Facebook's Yoga library.
- `LazyList` now has arguments for `margin` and cross-axis alignment
  (`verticalAlignment` for `LazyRow`, `horizontalAlignment` for `LazyColumn`)
- Remove the ability to use custom implementations of `LazyList`. Any missing functionality from
  the built-in versions should be filed as a feature request.
- The command-line tools (codegen, lint, schema) are now uploaded to Maven Central as standalone
  zip files in addition to each regular jar artifact for use with non-Gradle build systems.

Fixed:
- RTL layout direction is now supported by the Compose UI and View-based implementations of
  Redwood layout.

This version only works with Kotlin 1.8.22.


## [0.4.0] - 2023-06-09
[0.4.0]: https://github.com/cashapp/redwood/releases/tag/0.4.0

New:
- Experimental support for refresh indicators on `LazyRow` and `LazyColumn` via `refreshing` boolean
  and `onRefresh` lambda. These are experimental because we expect refresh support to migrate to
  some kind of future support for widget decorators so that it can be applied to any widget.
- `DisplayLinkClock` is available for iOS and MacOS users of Redwood.
  (Treehouse already had a frame clock for iOS).
- A `WidgetValue` (or `List<WidgetValue>`) produced from the generated testing function's
  `awaitSnapshot()` can now be converted to a `SnapshotChangeList` which can be serialized to JSON.
  That JSON can then later be deserialized and applied to a `TreehouseView` to recreate a full view
  hierarchy from any state. This is useful for unit testing widget implementations, screenshot
  testing, and more.
- Widget implementations can implement the `ChangeListener` interface to receive an `onEndChanges()`
  callback which occurs after all property or event lambda changes in that batch. This can help
  reduce thrashing in response to changes to multiple properties or event lambdas at once.
- `LazyRow` and `LazyColumn` now support a `placeholder` composable slot which will be used with
  Treehouse when a new item is displayed but before its content has loaded. Additionally, the size
  of these widgets can now be controlled through `width` and `height` constraints.

Changes:
- `LayoutModifier` has been renamed to `Modifier`.
- UI primitives like `Dp`, `Density`, and `Margin` have moved from Treehouse into the Redwood
  runtime (in the `app.cash.redwood.ui` package).
- `HostConfiguration` has moved from Treehouse into the Redwood runtime (in the
  `app.cash.redwood.ui` package) and is now called `UiConfiguration`.
- Composables running in Treehouse now run on a background thread on iOS. Previously they were
  running on the main thread. Interactions with UIKit still occur on the main thread.
- `RedwoodContent` function for hosting a Redwood composable within Compose UI has moved into a new
  `redwood-composeui` artifact as it will soon require a Compose UI dependency.
- The generated testing function now returns the value which was returned from the testing lambda.

  Before:
  ```kotlin
  suspend fun ExampleTester(body: suspend TestRedwoodComposition.() -> Unit)
  ```

  Now:
  ```kotlin
  suspend fun <R> ExampleTester(body: suspend TestRedwoodComposition.() -> R): R
  ```
- The Redwood and Treehouse frame clocks now send actual values for the frame time instead of 0.

Fixed:
- Widgets which accept nullable lambdas for events now receive an initial `null` value when no
  lambda is set. Previously a `null` would only be seen after a non-`null` lambda.
- Reduce binary impact of each widget's composable function by eliminating a large error string
  generated by the Kotlin compiler for an error case whose occurrence was impossible.
- The iOS implementation of `Row`, `Column`, `Spacer`, and `UIViewChildren` now react to size and
  child view changes more accurately according to UIKit norms.

This version only works with Kotlin 1.8.20.


## [0.3.0] - 2023-05-15
[0.3.0]: https://github.com/cashapp/redwood/releases/tag/0.3.0

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
[0.2.1]: https://github.com/cashapp/redwood/releases/tag/0.2.1

Changed:
- Do not use a `ScrollView`/`HorizontalScrollView` as the parent container for View-based `Row` and
  `Column` display when the container is not scrollable (the default). Use a `FrameLayout` instead.

Fixed:
- Actually publish the `redwood-treehouse-composeui` artifact.

This version only works with Kotlin 1.7.20.


## [0.2.0] - 2023-01-30
[0.2.0]: https://github.com/cashapp/redwood/releases/tag/0.2.0

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
[0.1.0]: https://github.com/cashapp/redwood/releases/tag/0.1.0

Initial release.

This version only works with Kotlin 1.7.20.
