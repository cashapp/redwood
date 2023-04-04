package app.cash.redwood.yoga.internal.detail

import app.cash.redwood.yoga.internal.YGConfig
import app.cash.redwood.yoga.internal.YGNode
import app.cash.redwood.yoga.internal.enums.YGLogLevel

//struct YGNode;
//struct YGConfig;
object Log {
    fun log(node: YGNode?, level: YGLogLevel, context: Any?, format: String, vararg args: Any?) {
        GlobalMembers.vlog(node?.getConfig(), node, level, context, format, *args)
    }

    fun log(
      config: YGConfig?,
      level: YGLogLevel,
      context: Any?,
      format: String,
      vararg args: Any?
    ) {
        GlobalMembers.vlog(config, null, level, context, format, *args)
    }
}
