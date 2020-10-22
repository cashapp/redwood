package example.sunspot.client

import app.cash.treehouse.protocol.PropertyDiff

interface SunspotText<out T : Any> : SunspotNode<T> {
  fun text(text: String?)
  fun color(color: String)

  override fun apply(diff: PropertyDiff) {
    when (val tag = diff.tag) {
      1 -> text(diff.value.toString() /* TODO serialization call */)
      2 -> color(diff.value.toString() /* TODO serialization call */)
      else -> throw IllegalArgumentException("Unknown tag $tag")
    }
  }
}
