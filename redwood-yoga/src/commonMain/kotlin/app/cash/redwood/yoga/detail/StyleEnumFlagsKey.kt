package app.cash.redwood.yoga.detail

import kotlin.reflect.KClass

data class StyleEnumFlagsKey(
    val enumClazz: KClass<*>,
    val index: Int
)
