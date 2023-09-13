# Redwood

Redwood is a library for building reactive Android, iOS, and web UIs using Kotlin.

**Redwood is currently under development and not ready for use by anyone.**


### Reactive UIs

Android and iOS UI frameworks model the user interface as a ‘mutable view tree’ or document object
model (DOM). To build an application using the mutable view tree abstraction, the programmer
performs two discrete steps:

 * **Build the static view tree.** In Android the conventional tool for this is layout XML, though
   we've done some cool work with [Contour][contour] to build view trees with Kotlin lambdas.

 * **Make it dance.** The view tree should change in response to user actions (like pushing buttons)
   and external events (like data loading). The program mutates the view tree to represent the
   current application state. Some mutations change the on-screen UI instantly; others animate
   smoothly from the old state to the new state.

[React][react_js] popularized a new programming model, reactive UIs. With reactive UIs, the
programmer writes a `render()` function that accepts the application state and returns a view tree.
The framework calls this function with the initial application state and again each time the
application state changes. The framework analyzes the differences between pairs of view trees and
updates the display, including animating transitions where appropriate.

In React the view tree returned by the render function is called a _virtual DOM_, and it has an
on-screen counterpart called the _real DOM_. The virtual DOM is a tree of simple JavaScript value
objects; the real DOM is a tree of live browser HTML components. Creating and traversing thousands
of virtual DOM objects is fast; creating thousands of HTML components is not! Therefore, the virtual
DOM optimization is the magic that makes React work.


### Compose

[Jetpack Compose][compose] is an implementation of the reactive UI model for Android. It uses an
implementation trick to further optimize the reactive programming model. It is implemented in two
complementary modules:

 * **The Compose compiler** is a Kotlin compiler plugin that supports partial re-evaluation of a
   function. The programmer still writes render functions to transform application state into a view
   tree. The compiler rewrites this function to track which inputs yield which outputs. When the
   input application state changes, it evaluates only what is necessary to generate the
   corresponding view tree changes.

 * **Compose UI** is a new set of Android UI components designed to work with the Compose compiler.
   It addresses longstanding technical debt with Android's view system.

A Kotlin function that is rewritten by the Compose compiler is called a _composable function_.
Partial re-evaluation of a composable function is called _recomposing_.

Note that the Compose compiler can be used without Compose UI. For example, [compose-server-side]
renders HTML components on a server that are sent to a browser over a WebSocket.


### Design Systems

In Cash App we use a design system. It specifies our UI in detail and names its elements:

 * Names for our standard colors, fonts, icons, dimensions
 * Named text blocks, specified using the names above
 * Named controls, such as our standard checkboxes, buttons, and dialogs

The design system helps with collaboration between programmers and designers. It also increases
uniformity within the application and across platforms.


### What Is Redwood?

Redwood integrates the Compose compiler, a design system, and a set of platform-specific displays.
Each Redwood project is implemented in three parts:

 * **A design system.** Redwood includes a sample design system called ‘Sunspot’. Most
   applications should customize this to match their product needs.

 * **Displays for UI platforms.** The display draws the pixels of the design system on-screen.
   Displays can be implemented for any UI platform. Redwood includes sample displays for Sunspot
   for Android, iOS, and web.

 * **Composable Functions.** This is client logic that accepts application state and returns
   elements of the design system. These have similar responsibilities to presenters in an MVP
   system.


### Why Redwood?

We're eager to start writing reactive UIs! But we're reluctant to continue duplicating code across
iOS, Android, and web platforms. In particular, we don't like how supporting multiple platforms
reduces our overall agility.

We'd like to shortcut the slow native UI development process. Iterating on UIs for Android requires
a slow compile step and a slow `adb install` step. With Redwood, we hope to use the web as our
development target while we iterate on composable function changes.

We want the option to change application behavior without waiting for users to update their apps.
With Kotlin/JS we may be able to update our composable functions at application launch time, and run
them in a JavaScript VM. We may even be able to use [WebAssembly][webassembly] to accomplish this
with little performance penalty.


**Redwood is a library, not a framework.** It is designed to be adopted incrementally, and to
be low-risk to integrate in an existing Android project. Using Redwood in an iOS or web
application is riskier! We've had good experiences with [Kotlin Multiplatform Mobile][kmm], and
expect a similar outcome with Redwood.


### Code Sample

We start by expressing our design system as a set of Kotlin data classes. Redwood will use these
classes to generate type-safe APIs for the displays and composable functions.

```kotlin
@Widget(1)
data class Text(
  @Property(1) val text: String?,
  @Property(2) @Default("\"black\"") val color: String,
)

@Widget(2)
data class Button(
  @Property(1) val text: String?,
  @Property(2) @Default("true") val enabled: Boolean,
  @Property(3) val onClick: () -> Unit,
)
```

Displays implement the design system using native UI components.

```kotlin
class AndroidText(
  override val value: TextView,
) : Text<View> {
  override fun text(text: String?) {
    value.text = text
  }

  override fun color(color: String) {
    value.setTextColor(Color.parseColor(color))
  }
}
```

Composable functions render application state into the design system. These will make use of Compose
API features like `remember()`.

```kotlin
@Composable
fun Counter(value: Int = 0) {
  var count by remember { mutableStateOf(value) }

  Button("-1", onClick = { count-- })
  Text(count.toString())
  Button("+1", onClick = { count++ })
}
```

### Version compatibility

Since Kotlin compiler plugins are an unstable API, certain versions of Redwood only work with
certain versions of Kotlin.

| Kotlin | Redwood       |
|--------|---------------|
| 1.9.10 | 0.7.0         |
| 1.9.0  | 0.6.0         |
| 1.8.22 | 0.5.0         |
| 1.8.20 | 0.3.0 - 0.4.0 |
| 1.7.20 | 0.1.0 - 0.2.1 |

### Custom Compose Compiler

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


[compose-server-side]: https://github.com/ShikaSD/compose-server-side
[compose]: https://developer.android.com/jetpack/compose
[contour]: https://github.com/cashapp/contour
[kmm]: https://kotlinlang.org/lp/mobile/
[react_js]: https://reactjs.org/
[webassembly]: https://webassembly.org/
