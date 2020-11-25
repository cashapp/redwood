package example.counter

import app.cash.treehouse.schema.Container
import app.cash.treehouse.schema.Default
import app.cash.treehouse.schema.Node
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.Tag

@Schema([
  Row::class,
  Column::class,
  CounterText::class,
  CounterButton::class,
])
interface Counter

@Container
interface Row

@Container
interface Column

@Node(1)
data class CounterText(
  @Tag(1) val text: String?,
  @Tag(2) @Default("\"black\"") val color: String,
)

@Node(2)
data class CounterButton(
  @Tag(1) val text: String?,
  @Tag(2) @Default("true") val enabled: Boolean,
  @Tag(3) val onClick: () -> Unit,
)
