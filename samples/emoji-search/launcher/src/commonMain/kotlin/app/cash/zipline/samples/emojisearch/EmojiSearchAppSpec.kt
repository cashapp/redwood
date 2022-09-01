package app.cash.zipline.samples.emojisearch

import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.ViewBinder
import app.cash.zipline.*
import kotlinx.coroutines.flow.flowOf

class EmojiSearchAppSpec(
  manifestUrlString: String,
  private val hostApi: HostApi,
  override val viewBinderAdapter : ViewBinder.Adapter,
) : TreehouseApp.Spec<EmojiSearchPresenter>() {
  override val name = "emoji-search"
  override val manifestUrl = flowOf(manifestUrlString)

  override fun bindServices(zipline: Zipline) {
    zipline.bind<HostApi>("HostApi", hostApi)
  }

  override fun create(zipline: Zipline): EmojiSearchPresenter {
    return zipline.take<EmojiSearchPresenter>("EmojiSearchPresenter")
  }
}
