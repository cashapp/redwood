package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.enums.YGExperiment
import app.cash.redwood.yoga.internal.enums.YGLogLevel
import app.cash.redwood.yoga.internal.interfaces.YGCloneNodeFunc
import app.cash.redwood.yoga.internal.interfaces.YGLogger

class YGConfig(logger: YGLogger?) //Type originates from: YGConfig.h
{
    private val logger_struct = logger_Struct()
    var useWebDefaults = false
    var useLegacyStretchBehaviour = false
    var shouldDiffLayoutWithoutLegacyStretchBehaviour = false
    var printTree = false
    var pointScaleFactor = 1.0f
    val experimentalFeatures = ArrayList<Boolean>()
    var context: Any? = null
    private var cloneNodeCallback_struct: cloneNodeCallback_Struct? = cloneNodeCallback_Struct()
    private var cloneNodeUsesContext_ = false
    private var loggerUsesContext_: Boolean

    init  //Method definition originates from: YGConfig.cpp
    {
        cloneNodeCallback_struct = null
        logger_struct.noContext = logger
        loggerUsesContext_ = false
        for (i in YGExperiment.values().indices) {
            experimentalFeatures.add(false)
        }
    }

    fun log(
      config: YGConfig?,
      node: YGNode?,
      logLevel: YGLogLevel,
      logContext: Any?,
      format: String,
      vararg args: Any?
    ) //Method definition originates from: YGConfig.cpp
    {
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
      cloneContext: Any?
    ): YGNode //Method definition originates from: YGConfig.cpp
    {
        var clone: YGNode? = null
        if (cloneNodeCallback_struct!!.noContext != null) {
            clone = if (cloneNodeUsesContext_) cloneNodeCallback_struct!!.withContext!!.invoke(
                node, owner, childIndex,
                cloneContext
            ) else cloneNodeCallback_struct!!.noContext!!.invoke(node, owner, childIndex)
        }
        if (clone == null) {
            clone = GlobalMembers.YGNodeClone(node)
        }
        return clone
    }

    fun setCloneNodeCallback(cloneNode: YGCloneNodeFunc?) {
        cloneNodeCallback_struct!!.noContext = cloneNode
        cloneNodeUsesContext_ = false
    }

    fun setCloneNodeCallback(cloneNode: CloneWithContextFn?) {
        cloneNodeCallback_struct!!.withContext = cloneNode
        cloneNodeUsesContext_ = true
    }

    fun setCloneNodeCallback() {
        cloneNodeCallback_struct!!.noContext = null
        cloneNodeUsesContext_ = false
    }

    fun interface LogWithContextFn {
        operator fun invoke(
          config: YGConfig?,
          node: YGNode?,
          level: YGLogLevel?,
          context: Any?,
          format: String?,
          vararg args: Any?
        ): Int
    }

    fun interface CloneWithContextFn {
        operator fun invoke(
          node: YGNode?,
          owner: YGNode?,
          childIndex: Int,
          cloneContext: Any?
        ): YGNode
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
