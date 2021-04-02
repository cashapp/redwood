package example.sunspot

import app.cash.treehouse.schema.Children
import app.cash.treehouse.schema.Default
import app.cash.treehouse.schema.Node
import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema

@Schema([
  SunspotBox::class,
  SunspotText::class,
  SunspotButton::class,
])
interface Sunspot

@Node(1)
data class SunspotBox(
  @Property(1) val orientation: Boolean, // TODO enum or whatever
  @Children(1) val children: List<Any>,
)

@Node(2)
data class SunspotText(
  @Property(1) val text: String?,
  @Property(2) @Default("\"black\"") val color: String,
)

@Node(3)
data class SunspotButton(
  @Property(1) val text: String?,
  @Property(2) @Default("true") val enabled: Boolean,
  @Property(3) val onClick: () -> Unit,
)
