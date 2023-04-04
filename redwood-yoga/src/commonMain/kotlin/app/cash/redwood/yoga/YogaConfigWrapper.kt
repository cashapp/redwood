/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YogaExperimentalFeature
import app.cash.redwood.yoga.interfaces.YogaLogger
import app.cash.redwood.yoga.internal.GlobalMembers
import app.cash.redwood.yoga.internal.YGConfig
import app.cash.redwood.yoga.internal.enums.YGExperimentalFeature

class YogaConfigWrapper private constructor(var mNativePointer: YGConfig) : YogaConfig() {
    private var mLogger: YogaLogger? = null

    internal constructor() : this(GlobalMembers.YGConfigNew()) {}

    override fun setExperimentalFeatureEnabled(feature: YogaExperimentalFeature, enabled: Boolean) {
        GlobalMembers.YGConfigSetExperimentalFeatureEnabled(
            mNativePointer,
            YGExperimentalFeature.Companion.forValue(feature.intValue()), enabled
        )
    }

    override fun setUseWebDefaults(useWebDefaults: Boolean) {
        GlobalMembers.YGConfigSetUseWebDefaults(mNativePointer, useWebDefaults)
    }

    override fun setPrintTreeFlag(enable: Boolean) {
        GlobalMembers.YGConfigSetPrintTreeFlag(mNativePointer, enable)
    }

    override fun setPointScaleFactor(pixelsInPoint: Float) {
        GlobalMembers.YGConfigSetPointScaleFactor(mNativePointer, pixelsInPoint)
    }

    /**
     * Yoga previously had an error where containers would take the maximum space possible instead of the minimum
     * like they are supposed to. In practice this resulted in implicit behaviour similar to align-self: stretch;
     * Because this was such a long-standing bug we must allow legacy users to switch back to this behaviour.
     */
    override fun setUseLegacyStretchBehaviour(useLegacyStretchBehaviour: Boolean) {
        GlobalMembers.YGConfigSetUseLegacyStretchBehaviour(
            mNativePointer,
            useLegacyStretchBehaviour
        )
    }

    /**
     * If this flag is set then yoga would diff the layout without legacy flag and would set a bool in
     * YogaNode(mDoesLegacyStretchFlagAffectsLayout) with true if the layouts were different and false
     * if not
     */
    override fun setShouldDiffLayoutWithoutLegacyStretchBehaviour(
        shouldDiffLayoutWithoutLegacyStretchBehaviour: Boolean
    ) {
        GlobalMembers.YGConfigSetShouldDiffLayoutWithoutLegacyStretchBehaviour(
            mNativePointer, shouldDiffLayoutWithoutLegacyStretchBehaviour
        )
    }

    override fun setLogger(logger: YogaLogger?) {
        mLogger = logger
        GlobalMembers.YGConfigSetLogger(mNativePointer, logger)
    }

    override fun getLogger(): YogaLogger? {
        return mLogger
    }

    public override fun getNativePointer(): YGConfig {
        return mNativePointer
    }
}
