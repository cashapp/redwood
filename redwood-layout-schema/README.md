# Redwood Layout

Redwood's layout system provides common layout primitives to support arranging and laying out
widgets. It provides widgets for:

- `Row`: Lays wigets out along the X axis (horizontally).
- `Column`: Lays widgets out along the Y axis (vertically).
- `Box`: Lays widgets out along the Z axis (on top of eachother).
- `Spacer`: Adds space between widgets.

Internally the layout system uses a common layout engine written in Kotlin Multiplatform. The
layout engine operates on a virtual DOM (document object model) composed of simple nodes
where each node is mapped to a widget in the real DOM. This lets the layout engine perform
operations on the DOM with consistent rendering across platforms. The system provides widget
bindings for for Android Views (`redwood-layout-view`), iOS UiKit (`redwood-layout-uiview`), and
Compose UI (`redwood-layout-composeui`).

## Widgets

### Row

Lays widgets out along the X axis (horizontally).

```kotlin
Row {
  Text("One")
  Text("Two")
  Text("Three")
}
```

<p style="text-align: center;">
    <img src="docs_images/row1.png">
</p>

### Column

Lays widgets out along the Y axis (vertically).

```kotlin
Column {
  Text("One")
  Text("Two")
  Text("Three")
}
```

<p style="text-align: center;">
    <img src="docs_images/column1.png">
</p>

### Box

Lays widgets out along the Z axis (on top of eachother). Widgets are laid from lowest Z index
(first) to highest Z index (last).

```kotlin
Box {
  Color(
    color = Red,
    modifier = Modifier.size(24.dp),
  )
  Color(
    color = Green,
    modifier = Modifier.size(16.dp),
  )
  Color(
    color = Blue,
    modifier = Modifier.size(8.dp),
  )
}
```

<p style="text-align: center;">
    <img src="docs_images/box1.png" width="250" height="250">
</p>

### Spacer

Adds space between widgets.

```kotlin
Column {
  Text("Top")
  Spacer(height = 24.dp)
  Text("Bottom")
}
```

<p style="text-align: center;">
  <img src="docs_images/spacer1.png">
</p>

## Density

In the Redwood layout system, sizes are defined using screen density independent values, `Dp`. By using density independent values we ensure that our widgets look the same size on different devices and different platforms. These values can then be converted into values for a device's coordinate system using a `Density`. For example:

```kotlin
val width = 5.dp
val widthPx = with(density) { width.toPx() }
```

The `density` object is created differently depending on the platform.

- On Android, you can create a density object using `Density(resources)`.
- On iOS, you can get a density object using `Density.Default`.
    - We don't need to create a custom density object as iOS already handles density in their coordinate system by default.
- In Compose UI, you can create a density object using `Density(LocalDensity.current.density.toDouble())`.
