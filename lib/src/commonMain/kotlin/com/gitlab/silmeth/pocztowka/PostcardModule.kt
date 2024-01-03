package com.gitlab.silmeth.pocztowka

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule

fun postcardModule(module: SerializersModule? = null): SerializersModule = SerializersModule {
    if (module != null) {
        include(module)
    }

    contextual(UShort::class, UShort.serializer())
    contextual(UInt::class, UInt.serializer())
    contextual(ULong::class, ULong.serializer())
}
