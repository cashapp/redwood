package example.treehouse

import app.cash.treehouse.schema.Children
import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.Widget

@Schema(
  [
    Box::class,
    Text::class,
    Button::class,
  ]
)
interface ExampleSchema

@Widget(1)
data class Box(
  @Children(1) val children: List<Any>,
)

@Widget(2)
data class Text(
  @Property(1) val text: String?,
)

@Widget(3)
data class Button(
  @Property(1) val text: String?,
  @Property(2) val onClick: () -> Unit,
)
