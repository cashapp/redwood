package app.cash.redwood.yoga

/** Type originates from: Utils.h */
class YGCollectFlexItemsRowValues {
  var itemsOnLine = 0
  var sizeConsumedOnCurrentLine = 0f
  var totalFlexGrowFactors = 0f
  var totalFlexShrinkScaledFactors = 0f
  var endOfLineIndex = 0
  val relativeChildren = ArrayList<YGNode>()
  var remainingFreeSpace = 0f
  var mainDim = 0f
  var crossDim = 0f
}
