/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YGLogLevel
import app.cash.redwood.yoga.interfaces.CloneWithContextFn
import app.cash.redwood.yoga.interfaces.LogWithContextFn
import app.cash.redwood.yoga.interfaces.YGCloneNodeFunc
import app.cash.redwood.yoga.interfaces.YGLogger

class YGConfig {
  private val logger_struct = logger_Struct()
  var useWebDefaults = false
  var useLegacyStretchBehaviour = false
  var shouldDiffLayoutWithoutLegacyStretchBehaviour = false
  var pointScaleFactor = 1.0f
  var context: Any? = null
  private var cloneNodeCallback_struct = cloneNodeCallback_Struct()
  private var cloneNodeUsesContext_ = false
  private var loggerUsesContext_ = false

  fun log(
    config: YGConfig?,
    node: YGNode?,
    logLevel: YGLogLevel,
    logContext: Any?,
    format: String,
    vararg args: Any?,
  ) {
    if (loggerUsesContext_) {
      logger_struct.withContext!!.invoke(config, node, logLevel, logContext, format, *args)
    } else {
      logger_struct.noContext!!.invoke(config, node, logLevel, format, *args)
    }
  }

  fun setLogger(logger: YGLogger?) {
    logger_struct.noContext = logger
    loggerUsesContext_ = false
  }

  fun setLogger(logger: LogWithContextFn?) {
    logger_struct.withContext = logger
    loggerUsesContext_ = true
  }

  fun setLogger() {
    logger_struct.noContext = null
    loggerUsesContext_ = false
  }

  fun cloneNode(
    node: YGNode,
    owner: YGNode?,
    childIndex: Int,
    cloneContext: Any?,
  ): YGNode {
    var clone: YGNode? = null
    if (cloneNodeCallback_struct.noContext != null) {
      clone = if (cloneNodeUsesContext_) {
        cloneNodeCallback_struct.withContext?.invoke(
          node = node,
          owner = owner,
          childIndex = childIndex,
          cloneContext = cloneContext,
        )
      } else {
        cloneNodeCallback_struct.noContext?.invoke(
          node = node,
          owner = owner,
          childIndex = childIndex,
        )
      }
    }
    if (clone == null) {
      clone = GlobalMembers.YGNodeClone(node)
    }
    return clone
  }

  fun setCloneNodeCallback(cloneNode: YGCloneNodeFunc?) {
    cloneNodeCallback_struct.noContext = cloneNode
    cloneNodeUsesContext_ = false
  }

  fun setCloneNodeCallback(cloneNode: CloneWithContextFn?) {
    cloneNodeCallback_struct.withContext = cloneNode
    cloneNodeUsesContext_ = true
  }

  fun setCloneNodeCallback() {
    cloneNodeCallback_struct.noContext = null
    cloneNodeUsesContext_ = false
  }

  internal class cloneNodeCallback_Struct {
    var withContext: CloneWithContextFn? = null
    var noContext: YGCloneNodeFunc? = null
  }

  internal class logger_Struct {
    var withContext: LogWithContextFn? = null
    var noContext: YGLogger? = null
  }
}
