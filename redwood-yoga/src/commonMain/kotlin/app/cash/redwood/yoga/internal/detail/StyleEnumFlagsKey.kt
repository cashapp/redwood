package app.cash.redwood.yoga.internal.detail

import kotlin.reflect.KClass

data class StyleEnumFlagsKey(
    val enumClazz: KClass<*>,
    val index: Int
)
