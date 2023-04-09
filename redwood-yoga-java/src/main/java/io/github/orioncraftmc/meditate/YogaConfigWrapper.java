/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.orioncraftmc.meditate;

import io.github.orioncraftmc.meditate.enums.YogaExperimentalFeature;
import io.github.orioncraftmc.meditate.interfaces.YogaLogger;
import io.github.orioncraftmc.meditate.internal.GlobalMembers;
import io.github.orioncraftmc.meditate.internal.YGConfig;
import io.github.orioncraftmc.meditate.internal.enums.YGExperimentalFeature;

public class YogaConfigWrapper extends YogaConfig {

  YGConfig mNativePointer;
  private YogaLogger mLogger;

  private YogaConfigWrapper(YGConfig nativePointer) {
    mNativePointer = nativePointer;
  }

  YogaConfigWrapper() {
    this(GlobalMembers.YGConfigNew());
  }

  public void setExperimentalFeatureEnabled(YogaExperimentalFeature feature, boolean enabled) {
    GlobalMembers.YGConfigSetExperimentalFeatureEnabled(mNativePointer,
            YGExperimentalFeature.forValue(feature.intValue()), enabled);
  }

  public void setUseWebDefaults(boolean useWebDefaults) {
    GlobalMembers.YGConfigSetUseWebDefaults(mNativePointer, useWebDefaults);
  }

  public void setPrintTreeFlag(boolean enable) {
    GlobalMembers.YGConfigSetPrintTreeFlag(mNativePointer, enable);
  }

  public void setPointScaleFactor(float pixelsInPoint) {
    GlobalMembers.YGConfigSetPointScaleFactor(mNativePointer, pixelsInPoint);
  }

  /**
   * Yoga previously had an error where containers would take the maximum space possible instead of the minimum
   * like they are supposed to. In practice this resulted in implicit behaviour similar to align-self: stretch;
   * Because this was such a long-standing bug we must allow legacy users to switch back to this behaviour.
   */
  public void setUseLegacyStretchBehaviour(boolean useLegacyStretchBehaviour) {
    GlobalMembers.YGConfigSetUseLegacyStretchBehaviour(mNativePointer, useLegacyStretchBehaviour);
  }

  /**
   * If this flag is set then yoga would diff the layout without legacy flag and would set a bool in
   * YogaNode(mDoesLegacyStretchFlagAffectsLayout) with true if the layouts were different and false
   * if not
   */
  public void setShouldDiffLayoutWithoutLegacyStretchBehaviour(
      boolean shouldDiffLayoutWithoutLegacyStretchBehaviour) {
      GlobalMembers.YGConfigSetShouldDiffLayoutWithoutLegacyStretchBehaviour(
          mNativePointer, shouldDiffLayoutWithoutLegacyStretchBehaviour);
  }

  public void setLogger(YogaLogger logger) {
    mLogger = logger;
    GlobalMembers.YGConfigSetLogger(mNativePointer, logger);
  }

  public YogaLogger getLogger() {
    return mLogger;
  }

  YGConfig getNativePointer() {
    return mNativePointer;
  }
}
