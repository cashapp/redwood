package example.sunspot

import app.cash.treehouse.schema.Children
import app.cash.treehouse.schema.Default
import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.Widget

@Schema([
  SunspotBox::class,
  SunspotText::class,
  SunspotButton::class,
])
interface Sunspot

@Widget(1)
data class SunspotBox(
  @Property(1) val orientation: Boolean, // TODO enum or whatever
  @Children(1) val children: List<Any>,
)

@Widget(2)
data class SunspotText(
  @Property(1) val text: String?,
  @Property(2) @Default("\"black\"") val color: String,
)

@Widget(3)
data class SunspotButton(
  @Property(1) val text: String?,
  @Property(2) @Default("true") val enabled: Boolean,
  @Property(3) val onClick: () -> Unit,
)
