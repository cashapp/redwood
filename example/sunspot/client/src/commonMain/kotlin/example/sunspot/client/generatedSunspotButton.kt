package example.sunspot.client

import app.cash.treehouse.protocol.PropertyDiff

interface SunspotButton<out T: Any> : SunspotNode<T> {
  fun text(text: String?)
  fun enabled(enabled: Boolean)
  fun onClick(onClick: Boolean)

  override fun apply(diff: PropertyDiff) {
    when (val tag = diff.tag) {
      1 -> text(diff.value.toString() /* TODO serialization call */)
      2 -> enabled(diff.value as Boolean /* TODO serialization call */)
      3 -> onClick(diff.value as Boolean /* TODO serialization call */)
      else -> throw IllegalArgumentException("Unknown tag $tag")
    }
  }
}
