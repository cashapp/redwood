package app.cash.treehouse.schema

import kotlin.reflect.KClass

annotation class Schema(val entities: Array<KClass<*>>)
