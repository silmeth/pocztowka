package com.gitlab.silmeth.pocztowka

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

fun <T> postcardEncode(serializer: SerializationStrategy<T>, value: T): ByteArray {
    val encoder = PostcardEncoder(serializersModule = postcardModule())
    encoder.encodeSerializableValue(serializer, value)
    return encoder.toBytes()
}

inline fun <reified T> postcardEncode(value: T): ByteArray {
    val encoder = PostcardEncoder(serializersModule = postcardModule())
    encoder.encodeSerializableValue(encoder.serializersModule.serializer(), value)
    return encoder.toBytes()
}

// TODO: polymorphism
class PostcardEncoder(
    val unsigned: Boolean = false,
    override val serializersModule: SerializersModule,
) : AbstractEncoder() {
    private val buffer = ByteBuffer()

    override fun encodeByte(value: Byte) {
        buffer.write(value)
    }

    override fun encodeBoolean(value: Boolean) =
        when (value) {
            true -> buffer.write(0x01)
            false -> buffer.write(0x00)
        }

    override fun encodeShort(value: Short) {
        val tmp = zigzag(value.toLong())
        encodeULong(tmp.toULong())
    }

    fun encodeUShort(value: UShort) = encodeULong(value.toULong())

    override fun encodeInt(value: Int) {
        val tmp = zigzag(value.toLong())
        encodeULong(tmp.toULong())
    }

    fun encodeUInt(value: UInt) = encodeULong(value.toULong())

    override fun encodeLong(value: Long): Unit {
        val tmp = zigzag(value)
        encodeULong(tmp.toULong())
    }

    fun encodeULong(value: ULong) {
        when {
            value <= Byte.MAX_VALUE.toULong() -> encodeByte(value.toByte())
            else -> {
                var remaining = value
                while (remaining > Byte.MAX_VALUE.toULong()) {
                    val lsb: Byte = ((remaining and 0x7fuL) or 0x80uL).toByte()
                    encodeByte(lsb)
                    remaining = remaining.shr(7)
                }
                encodeByte(remaining.toByte())
            }
        }
    }

    override fun encodeFloat(value: Float) {
        val bits = value.toRawBits()
        encodeByte((bits and 0x000000FF).toByte())
        encodeByte((bits.shr(8) and 0x000000FF).toByte())
        encodeByte((bits.shr(16) and 0x000000FF).toByte())
        encodeByte((bits.shr(24) and 0x000000FF).toByte())
    }

    override fun encodeDouble(value: Double) {
        val bits = value.toRawBits()
        encodeByte((bits and 0x00000000_000000FF).toByte())
        encodeByte((bits.shr(8) and 0x00000000_000000FF).toByte())
        encodeByte((bits.shr(16) and 0x00000000_000000FF).toByte())
        encodeByte((bits.shr(24) and 0x00000000_000000FF).toByte())
        encodeByte((bits.shr(32) and 0x00000000_000000FF).toByte())
        encodeByte((bits.shr(40) and 0x00000000_000000FF).toByte())
        encodeByte((bits.shr(48) and 0x00000000_000000FF).toByte())
        encodeByte((bits.shr(56) and 0x00000000_000000FF).toByte())
    }

    override fun encodeChar(value: Char) = encodeString(value.toString())

    override fun encodeString(value: String) {
        val asUtf8 = value.encodeToByteArray()
        encodeUInt(asUtf8.size.toUInt())
        buffer.append(asUtf8)
    }

    override fun encodeNotNullMark() {
        buffer.write(0x01)
    }

    override fun encodeNull() {
        buffer.write(0x00)
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder =
        if (descriptor.isInline &&
                descriptor.serialName in setOf("kotlin.UShort", "kotlin.UInt", "kotlin.ULong")
        ) {
            UnsignedEncoder(this)
        } else {
            this
        }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeUInt(index.toUInt())
    }

    override fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int
    ): PostcardEncoder {
        // TODO: should arrays be treated differently? (in Rust fixed-size arrays don’t serialize
        // their size)
        encodeUInt(collectionSize.toUInt())
        return this
    }

    override fun beginStructure(descriptor: SerialDescriptor): PostcardEncoder = this

    override fun encodeValue(value: Any) {
        throw SerializationException.UnsupportedType()
    }

    fun toBytes(): ByteArray = buffer.toArray()

    private fun zigzag(value: Long): Long = value.shl(1) xor value.shr(63)

    class UnsignedEncoder(private val inner: PostcardEncoder) : Encoder by inner {
        override fun encodeShort(value: Short) = inner.encodeUShort(value.toUShort())
        override fun encodeInt(value: Int) = inner.encodeUInt(value.toUInt())
        override fun encodeLong(value: Long) = inner.encodeULong(value.toULong())
    }
}

private class ByteBuffer(capacity: Int = 16) {
    private var size: Int = 0
    private var array: ByteArray = ByteArray(capacity)

    fun write(byte: Byte) {
        grow(size + 1)
        array[size++] = byte
    }

    fun append(bytes: ByteArray) {
        grow(size + bytes.size)
        bytes.copyInto(array, size)
        size += bytes.size
    }

    fun toArray(): ByteArray = array.sliceArray(0 until size)

    private fun grow(requested: Int) {
        var capacity = array.size
        while (capacity < requested) {
            capacity *= 2
        }

        if (capacity > array.size) {
            val newArray = ByteArray(capacity)
            array.copyInto(newArray)
            array = newArray
        }
    }
}
