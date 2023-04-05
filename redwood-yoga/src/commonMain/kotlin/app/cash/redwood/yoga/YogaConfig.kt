/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YogaExperimentalFeature
import app.cash.redwood.yoga.interfaces.YogaLogger
import app.cash.redwood.yoga.internal.YGConfig

abstract class YogaConfig {
    abstract fun setExperimentalFeatureEnabled(feature: YogaExperimentalFeature, enabled: Boolean)
    abstract fun setUseWebDefaults(useWebDefaults: Boolean)
    abstract fun setPrintTreeFlag(enable: Boolean)
    abstract fun setPointScaleFactor(pixelsInPoint: Float)

    /**
     * Yoga previously had an error where containers would take the maximum space possible instead of the minimum
     * like they are supposed to. In practice this resulted in implicit behaviour similar to align-self: stretch;
     * Because this was such a long-standing bug we must allow legacy users to switch back to this behaviour.
     */
    abstract fun setUseLegacyStretchBehaviour(useLegacyStretchBehaviour: Boolean)

    /**
     * If this flag is set then yoga would diff the layout without legacy flag and would set a bool in
     * YogaNode(mDoesLegacyStretchFlagAffectsLayout) with true if the layouts were different and false
     * if not
     */
    abstract fun setShouldDiffLayoutWithoutLegacyStretchBehaviour(
        shouldDiffLayoutWithoutLegacyStretchBehaviour: Boolean
    )

    abstract fun setLogger(logger: YogaLogger?)
    abstract fun getLogger(): YogaLogger?
    abstract fun getNativePointer(): YGConfig

    companion object {
        var SPACING_TYPE = 1
    }
}
