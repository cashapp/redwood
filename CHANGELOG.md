# Change Log

## [Unreleased]


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



[Unreleased]: https://github.com/cashapp/redwood/compare/0.2.0...HEAD
[0.2.0]: https://github.com/cashapp/redwood/releases/tag/0.2.0
[0.1.0]: https://github.com/cashapp/redwood/releases/tag/0.1.0
