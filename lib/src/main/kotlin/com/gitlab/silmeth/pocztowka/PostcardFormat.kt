package com.gitlab.silmeth.pocztowka

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule

class Postcard(module: SerializersModule? = null) : BinaryFormat {
    override val serializersModule: SerializersModule = postcardModule(module)

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val encoder = PostcardEncoder(serializersModule = serializersModule)
        encoder.encodeSerializableValue(serializer, value)
        return encoder.toBytes()
    }

    override fun <T> decodeFromByteArray(
        deserializer: DeserializationStrategy<T>,
        bytes: ByteArray
    ): T {
        val decoder = PostcardDecoder(bytes, serializersModule = serializersModule)
        return decoder.decodeSerializableValue(deserializer)
    }
}
