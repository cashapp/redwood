/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.orioncraftmc.meditate;

import io.github.orioncraftmc.meditate.internal.YGSize;

/**
 * Helpers for building measure output value.
 */
public class YogaMeasureOutput {

  public static YGSize make(float width, float height) {
    return new YGSize(width, height);
  }

  public static YGSize make(int width, int height) {
    return make((float) width, (float) height);
  }

  public static float getWidth(YGSize measureOutput) {
    return measureOutput.width;
  }

  public static float getHeight(YGSize measureOutput) {
    return measureOutput.height;
  }
}
