/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.orioncraftmc.meditate;

public abstract class YogaConfigFactory {
  public static YogaConfig create() {
    return new YogaConfigWrapper();
  }
}
