/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.orioncraftmc.meditate;

public abstract class YogaNodeFactory {
  public static YogaNode create() {
    return new YogaNodeWrapper();
  }

  public static YogaNode create(YogaConfig config) {
    return new YogaNodeWrapper(config);
  }
}
