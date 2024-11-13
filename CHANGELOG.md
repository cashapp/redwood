# Change Log

## [Unreleased]
[Unreleased]: https://github.com/cashapp/redwood/compare/0.15.0...HEAD

New:
- Redwood publishes what's happening in bound content through the new `Content.State` type.
- Accept a `ZiplineHttpClient` in `TreehouseAppFactory` on Android.

Changed:
- Drop support for non-incremental layouts in `Row` and `Column`.
- Support for `@Default` annotation has now been removed, as detailed in the 0.15.0 release.

Fixed:
- Fix a layout bug where children of fixed-with `Row` containers were assigned the wrong width.
- Fix inconsistencies between iOS and Android for `Column` and `Row` layouts.
- Fix a layout bug where `Row` and `Column` layouts reported the wrong dimensions if their subviews could wrap.
- Correctly update the layout when a Box's child's modifiers are removed.
- Fix a layout bug where children of `Box` containers were not measured properly.
- Fix a bug where `LazyColumn` didn't honor child widget resizes.

Breaking:
- Replace `CodeListener` with a new `DynamicContentWidgetFactory` API. Now loading and crashed views work like all other child widgets.


## [0.15.0] - 2024-09-30
[0.15.0]: https://github.com/cashapp/redwood/releases/tag/0.15.0

New:
- Default expressions can now be used directly in the schema rather than using the `@Default` annotation. The annotation has been deprecated, and will be removed in the next release.
- `EventListener.Factory.close()` is called by `TreehouseApp.close()` to release any resources held by the factory.
- Lambda parameter names defined in the schema are now propagated to the generated composable and widget interface.
- `ResizableWidget` is an interface that `UIView` widgets must use if their intrinsic sizes may change dynamically. It notifies any enclosing parent views to trigger a new layout.

Changed:
- Removed Wasm JS target. We are not ready to support it yet.

Fixed:
- Breaking the last remaining retain cycle in `UIViewLazyList`.
- Don't leak the `DisplayLink` when a `TreehouseApp` is stopped on iOS.
- Correctly handle dynamic size changes for child widgets of `Box`, `Column`, and `Row`.
- Don't clip elements of `Column` and `Row` layouts whose unbounded size exceeds the container size.
- Correctly implement margins for `Box` on iOS.
- Correctly handle dynamic updates to modifiers on `Column` and `Row`.


## [0.14.0] - 2024-08-29
[0.14.0]: https://github.com/cashapp/redwood/releases/tag/0.14.0

New:
- Source-based schema parser is now the default. The `useFir` Gradle property has been removed.
- `TreehouseAppFactory` accepts a `LeakDetector` which can be used to notify you of reference leaks for native UI nodes, Zipline instances, Redwood's own internal wrappers, and more.
- Introduce a `LoadingStrategy` interface to manage `LazyList` preloading.
- Optimize encoding modifiers in Kotlin/JS.

Changed:
- In Treehouse, events from the UI are now serialized on a background thread. This means that there is both a delay and a thread change between when a UI binding sends an event and when that object is converted to JSON. All arguments to events must not be mutable and support property reads on any thread. Best practice is for all event arguments to be completely immutable.
- `ProtocolFactory` interface is now sealed as arbitrary subtypes were never supported. Only schema-generated subtypes should be used.
- `UIViewLazyList` doesn't crash with a `NullPointerException` if cells are added, removed, and re-added without being reused.
- Change `UiConfiguration.viewportSize` to be nullable. A null `viewportSize` indicates the viewport's size has not been resolved yet.

Fixed:
- Breaking `content: UIView` retain cycle in `UIViewLazyList`'s `LazyListContainerCell`.
- Update `ProtocolNode` widget IDs when recycling widgets. This was causing pooled nodes to be leaked.

Breaking:
- The `TreehouseApp.spec` property is removed. Most callers should be able to use `TreehouseApp.name` instead. This is necessary to avoid a retain cycle.

Upgraded:
- Kotlin 2.0.20
- Zipline 1.17.0


## [0.13.0] - 2024-07-25
[0.13.0]: https://github.com/cashapp/redwood/releases/tag/0.13.0

New:
- Wasm JS added as a target for common Redwood modules. There is no Treehouse support today.
- Add `onScroll` property to `Row` and `Column`. This property is invoked when `overflow = Overflow.Scroll` and the container is scrolled.
- Add `Px` class to represent a raw pixel value in the host's coordinate system.
- New source-based schema parser can be enabled with `redwood { useFir = true }` in your schema module. Please report and failures to the issue tracker. This parser will become the default in 0.14.0.

Changed:
- The `TreehouseApp` type is now an abstract class. This should make it easier to write unit tests for code that integrates Treehouse.
- The `TreehouseApp.Spec.bindServices()` function is now suspending.
- The `TreehouseAppFactory` function now accepts a Zipline `LoaderEventListener` parameter.

Fixed:
- Using a `data object` for a widget of modifier no longer causes schema parsing to crash.
- Ensuring `LazyList`'s `itemsBefore` and `itemsAfter` properties are always within `[0, itemCount]`, to prevent `IndexOutOfBoundsException` crashes.
- Don't crash in `LazyList` when a scroll and content change occur in the same update.
- Updating a flex container's margin now works correctly for Yoga-based layouts.

Breaking:
- The `TreehouseApp.Factory.dispatchers` property is removed, and callers should migrate to `TreehouseApp.dispatchers`. With this update each `TreehouseApp` has its own private thread so a shared `dispatchers` property no longer fits our implementation.
  -`TreehouseApp.Spec.bindServices()` now accepts a `TreehouseApp` parameter.

Upgraded:
- Zipline 1.16.0


## [0.12.0] - 2024-06-18
[0.12.0]: https://github.com/cashapp/redwood/releases/tag/0.12.0

New:
- Upgrade to Kotlin 2.0!
- Added a basic DOM-based `LazyList` implementation.
-`TreehouseApp.close()` stops the app and prevents it from being started again later.
- Added `UiConfiguration.layoutDirection` to support reading the host's layout direction.
- New `redwood-bom` artifact can be used to ensure all Redwood artifacts use the same version. See [Gradle's documentation](https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import) on how to use the BOM in your build.

Changed:
- The `app.cash.redwood` Gradle plugin has been removed. This plugin did two things: apply the Compose compiler and add a dependency on the `redwood-compose` artifact. The Compose compiler can now be added by applying the `org.jetbrains.kotlin.plugin.compose` Gradle plugin. Dependencies on Redwood artifacts can be added manually.
- Removed deprecated `typealias`es for generated `-WidgetFactories` type which was renamed to `-WidgetSystem` in 0.10.0.
- Removed deprecated `Modifier.flex` extension function which is now supported natively by `Row` and `Column` since 0.8.0.
- Removed deprecated `TreehouseWidgetView` and `TreehouseUIKitView` type aliases for `TreehouseLayout` and `TreehouseUIView` which were renamed in 0.7.0.
- Removed deprecated `TreehouseAppFactory` functions with the old `FileSystem` and `Path` order which were changed in 0.11.0.
- Rename the two types named `ProtocolBridge` to `ProtocolHost` and `ProtocolGuest`.

Fixed:
- Fix memory leaks caused by reference cycles on iOS. We got into trouble mixing garbage-collected Kotlin objects with reference-counted Swift objects.

Breaking:
-`TreehouseApp.zipline` is now a `StateFlow<Zipline?>` instead of a `Zipline?`.
-`CodeListener.onCodeDetached()` replaces `onUncaughtException()`. The new function is called
 whenever code stops driving a view for any reason. The new function accepts a `Throwable?` that is
 non-null if it's detached due to exception.
-`Content.awaitContent()` now accepts an optional `Int` parameter for the number of updates to
 observe before the function returns.
- MacOS targets have been removed from all modules.

Upgraded:
- Kotlin 2.0.0
- Zipline 1.13.0
- kotlinx.serialization 1.7.0


### Gradle plugin removed

This version of Redwood removes the custom Gradle plugin in favor of [the official JetBrains Compose compiler plugin](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compiler.html) which ships as part of Kotlin itself.
Each module in which you had previously applied the `app.cash.redwood` plugin should be changed to apply `org.jetbrains.kotlin.plugin.compose` instead.
The Redwood dependencies will no longer be added as a result of the plugin change, and so any module which references Redwoods APIs should add those dependencies explicitly.

For posterity, the Kotlin version compatibility table and compiler version customization for our old Redwood Gradle plugin will be archived here:

<details>
<summary>Redwood 0.12.0 Gradle plugin Kotlin compatibility table</summary>
<p>

Since Kotlin compiler plugins are an unstable API, certain versions of Redwood only work with
certain versions of Kotlin.

| Kotlin | Redwood       |
|--------|---------------|
| 1.9.24 | 0.11.0        |
| 1.9.23 | 0.10.0        |
| 1.9.22 | 0.8.0 - 0.9.0 |
| 1.9.10 | 0.7.0         |
| 1.9.0  | 0.6.0         |
| 1.8.22 | 0.5.0         |
| 1.8.20 | 0.3.0 - 0.4.0 |
| 1.7.20 | 0.1.0 - 0.2.1 |

</p>
</details>

<details>
<summary>Redwood 0.12.0 Gradle plugin Compose compiler customization instructions</summary>
<p>

Each version of Redwood ships with a specific JetBrains Compose compiler version which works with
a single version of Kotlin (see [version table](#version-compatibility) above). Newer versions of
the Compose compiler or alternate Compose compilers can be specified using the Gradle extension.

To use a new version of the JetBrains Compose compiler version:
```kotlin
redwood {
  kotlinCompilerPlugin.set("1.4.8")
}
```

To use an alternate Compose compiler dependency:
```kotlin
redwood {
  kotlinCompilerPlugin.set("com.example:custom-compose-compiler:1.0.0")
}
```

</p>
</details>


## [0.11.0] - 2024-05-15
[0.11.0]: https://github.com/cashapp/redwood/releases/tag/0.11.0

New:
- Added `toDebugString` method for `WidgetValue` and `List<WidgetValue>` which returns a formatted string of a widget's children and properties, useful for test debugging.

Changed:
- Removed generated `typealias`es for package names which changed in 0.10.0.
- In `UIViewLazyList`'s `UITableView`, adding special-case handling for programmatic scroll-to-top calls.
- APIs accepting a `FileSystem` and `Path` now have the `FileSystem` coming before the `Path` in the parameter list. Compatibility functions are retained for this version, but will be removed in the next version.
- Change `LazyListState` to be scroll-aware, reducing the size of the preload window while actively scrolling, and optimizing the preload window once the scroll has completed.

Fixed:
- Work around a problem with our memory-leak fix where our old LazyList code would crash when its placeholders were unexpectedly removed.
- Avoid calling into the internal Zipline instance from the UI thread on startup. This would manifest as weird native crashes due to multiple threads mutating shared memory.
- In `UIViewLazyList`, fix `UInt` to `UIColor` conversion math used for  `pullRefreshContentColor`.
- In `YogaUIView`'s `setScrollEnabled` method, only call `setNeedsLayout` if the `scrollEnabled` value is actually changing.
- In `YogaUIView`'s `layoutNodes` method, return early for nested `YogaUIView`s to prevent redundant frame calculations.

Upgraded:
- Zipline 1.10.1.

This version works with Kotlin 1.9.24 by default.


## [0.10.0] - 2024-04-05
[0.10.0]: https://github.com/cashapp/redwood/releases/tag/0.10.0

New:
- Compose UI implementation for `Box`.
- Layout modifier support for HTML DOM layouts.
- Unscoped modifiers provide a global hook for side-effecting behavior on native views. For example, create a background color modifier which changes the platform-native UI node through a factory function.
- `Widget.Children` interface now exposes `widgets: List<Widget<W>>` property. Most subtypes were already exposing this individually.

Changed:
- Disable klib signature clash checks for JS compilations. These occasionally occur as a result of Compose compiler behavior, and are safe to disable (the first-party JetBrains Compose Gradle plugin also disables them).
- `onModifierChanged` callback in `Widget.Children` now receives the index and the `Widget` instance affected by the change.
- The package of 'redwood-protocol-host' changed to `app.cash.redwood.protocol.host`. This should not affect end-users as its types are mostly for internal use.
- The entire `redwood-yoga` artifact's public API has been annotated with an opt-in annotation indicating that it's only for Redwood internal use and is not stable.
- Revert: Don't block touch events to non-subviews below a `Row`, `Column`, or `Box` in the iOS `UIView` implementation. This matches the behavior of the Android View and Compose UI implementations.
- The generated "widget factories" type (e.g., `MySchemaWidgetFactories`) is now called a "widget system" (e.g., `MySchemaWidgetSystem`). Sometimes it was also referred to as a "provider" in parameter names. A `@Deprecated typealias` is generated for now, but will be removed in the future.
- The package names of some generated code has changed. Deprecated `typealias`es are generated in the old locations for public types and functions, but those will be removed in the next release.
  - Testing code is now under `your.package.testing`.
  - Protocol guest code is now under `your.package.protocol.guest`.
  - Protocol host code is now under `your.package.protocol.host`.
- The 'app.cash.redwood.generator.compose.protocol' and 'app.cash.redwood.generator.widget.protocol' Gradle plugins are now deprecated and will be removed in the next release. Use 'app.cash.redwood.generator.protocol.guest' and 'app.cash.redwood.generator.protocol.host', respectively.
- The 'redwood-tooling-codegen' CLI flags for protocol codegen have changed from `--compose-protocol` and `--widget-protocol` to `--protocol-guest` and `--protocol-host`, respectively.
- Entrypoints to the protocol on the host-side and guest-side now require supplying the version of Redwood in use on the other side in order to ensure compatibility and work around any bugs in older versions. This uses a new `RedwoodVersion` type, and will be automatically wired if using our Treehouse artifacts.

Fixed:
- Fix failure to release JS resources when calling `CoroutineScope` is being cancelled
- JVM targets now correctly link against Java 8 APIs. Previously they produced Java 8 bytecode, but linked against the compile JDK's APIs (21). This allowed linking against newer APIs that might not exist on older runtimes, which is no longer possible. Android targets which also produce Java 8 bytecode were not affected.
- Fix the `View` implementation of `Box` to wrap its width and height by default. This matches the behavior of the `UIView` implementation and all other layout widgets.
- Fix the `UIView` implementation of `Box` not updating when some of its parameters are changed.
- Fix `Modifier.size` not being applied to children inside a `Box`.
- Fix `Margin` not being applied to the `UIView` implementation of `Box`.
- The `View` implementation of `Box` now applies start/end margins correctly in RTL, and does not crash if set before the native view was attached.
- Fix the backgroundColor for `UIViewLazyList` to be transparent. This matches the behavior of the other `LazyList` platform implementations.
- Fix `TreehouseUIView` to size itself according to the size of its subview.
- In `UIViewLazyList`, adding `beginUpdates`/`endUpdates` calls to `insertRows`/`deleteRows`, and wrapping changes in `UIView.performWithoutAnimation` blocks.
- Fix memory leak in 'protocol-guest' and 'protocol-host' where child nodes beneath a removed node were incorrectly retained in an internal map indefinitely. The guest protocol code has been updated to work around this memory leak when deployed to old hosts by sending individual remove operations for each node in the subtree.
- Ensure that Zipline services are not closed prematurely when disposing a Treehouse UI.
- In `UIViewLazyList`, don't remove subviews from hierarchy during `prepareForReuse` call

This version works with Kotlin 1.9.23 by default.


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
