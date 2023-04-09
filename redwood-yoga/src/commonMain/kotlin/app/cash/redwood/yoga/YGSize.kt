package app.cash.redwood.yoga

class YGSize(
  val width: Float,
  val height: Float,
)

fun YGSize(width: Int, height: Int): YGSize {
  return YGSize(width.toFloat(), height.toFloat())
}
