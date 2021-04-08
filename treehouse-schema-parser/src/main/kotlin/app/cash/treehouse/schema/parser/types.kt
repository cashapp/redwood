package app.cash.treehouse.schema.parser

import kotlin.reflect.KClass

internal val KClass<*>.packageName: String
  get() {
    // Replace with https://youtrack.jetbrains.com/issue/KT-18104 once it ships.
    // Note: Class.packageName isn't available until Java 9.
    val javaClass = java
    require(!javaClass.isPrimitive && !javaClass.isArray)
    return javaClass.name.substringBeforeLast(".", missingDelimiterValue = "")
  }
