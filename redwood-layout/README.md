Redwood Layout
=======

This artifact includes `Row` and `Column` widget implementations for Android Views, Android Compose UI, and iOS UIKit.

To being, add the follow entries to your `schema.kt`:

```kotlin
@Widget(1)
data class Row(
  @Property(1) @Default("Padding.Zero") val padding: Padding,
  @Property(2) @Default("Overflow.Clip") val overflow: Overflow,
  @Property(3) @Default("MainAxisAlignment.Start") val horizontalAlignment: MainAxisAlignment,
  @Property(4) @Default("CrossAxisAlignment.Start") val verticalAlignment: CrossAxisAlignment,
  @Children(5) val children: RowScope.() -> Unit,
)

object RowScope {
  /** https://developer.mozilla.org/en-US/docs/Web/CSS/flex-grow */
  fun LayoutModifier.grow(value: Int): LayoutModifier {
    return then(GrowLayoutModifier(value))
  }

  /** https://developer.mozilla.org/en-US/docs/Web/CSS/flex-shrink */
  fun LayoutModifier.shrink(value: Int): LayoutModifier {
    return then(ShrinkLayoutModifier(value))
  }

  /** https://developer.mozilla.org/en-US/docs/Web/CSS/align-self */
  fun LayoutModifier.verticalAlignment(alignment: CrossAxisAlignment): LayoutModifier {
    return then(VerticalAlignmentLayoutModifier(alignment))
  }
}

@Widget(2)
data class Column(
  @Property(1) @Default("Padding.Zero") val padding: Padding,
  @Property(2) @Default("Overflow.Clip") val overflow: Overflow,
  @Property(3) @Default("CrossAxisAlignment.Start") val horizontalAlignment: CrossAxisAlignment,
  @Property(4) @Default("MainAxisAlignment.Start") val verticalAlignment: MainAxisAlignment,
  @Children(5) val children: ColumnScope.() -> Unit,
)

object ColumnScope {
  /** https://developer.mozilla.org/en-US/docs/Web/CSS/flex-grow */
  fun LayoutModifier.grow(value: Int): LayoutModifier {
    return then(GrowLayoutModifier(value))
  }

  /** https://developer.mozilla.org/en-US/docs/Web/CSS/flex-shrink */
  fun LayoutModifier.shrink(value: Int): LayoutModifier {
    return then(ShrinkLayoutModifier(value))
  }

  /** https://developer.mozilla.org/en-US/docs/Web/CSS/align-self */
  fun LayoutModifier.horizontalAlignment(alignment: CrossAxisAlignment): LayoutModifier {
    return then(HorizontalAlignmentLayoutModifier(alignment))
  }
}
```

Then copy the following classes into the `commonMain` of your `widget` module. These classes connect
the generated schema with the platform `RowWidget` and `ColumnWidget` implementations:

```kotlin
class RowBridge<T : Any>(private val delegate: RowWidget<T>) : Row<T>, Widget<T> by delegate {
  override val children get() = delegate.children

  override fun padding(padding: Padding) = delegate.padding(padding)

  override fun overflow(overflow: Overflow) = delegate.overflow(overflow)

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) =
    delegate.horizontalAlignment(horizontalAlignment)

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) =
    delegate.verticalAlignment(verticalAlignment)
}

class ColumnBridge<T : Any>(private val delegate: ColumnWidget<T>) : Column<T>, Widget<T> by delegate {
  override val children get() = delegate.children

  override fun padding(padding: Padding) = delegate.padding(padding)

  override fun overflow(overflow: Overflow) = delegate.overflow(overflow)

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) =
    delegate.horizontalAlignment(horizontalAlignment)

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) =
    delegate.verticalAlignment(verticalAlignment)
}
```

Then connect it all together in the widget factories for each platform:

```kotlin
object AndroidTestSchemaWidgetFactory : TestSchemaWidgetFactory {
  fun Row() = RowBridge(ComposeRow())
  fun Column() = ColumnBridge(ComposeColumn())
}
```
