package example.counter

import app.cash.treehouse.schema.Children
import app.cash.treehouse.schema.Default
import app.cash.treehouse.schema.Node
import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema

@Schema([
  CounterBox::class,
  CounterText::class,
  CounterButton::class,
])
interface Counter

@Node(1)
data class CounterBox(
  @Property(1) val orientation: Boolean, // TODO enum or whatever
  @Children val children: List<Any>,
)

@Node(2)
data class CounterText(
  @Property(1) val text: String?,
  @Property(2) @Default("\"black\"") val color: String,
)

@Node(3)
data class CounterButton(
  @Property(1) val text: String?,
  @Property(2) @Default("true") val enabled: Boolean,
  @Property(3) val onClick: () -> Unit,
)
