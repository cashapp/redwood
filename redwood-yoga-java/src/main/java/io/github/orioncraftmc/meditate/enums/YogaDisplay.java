/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.orioncraftmc.meditate.enums;

public enum YogaDisplay {
  FLEX(0),
  NONE(1);

  private final int mIntValue;

  YogaDisplay(int intValue) {
    mIntValue = intValue;
  }

  public int intValue() {
    return mIntValue;
  }

  public static YogaDisplay fromInt(int value) {
    switch (value) {
      case 0: return FLEX;
      case 1: return NONE;
      default: throw new IllegalArgumentException("Unknown enum value: " + value);
    }
  }
}
