package com.gitlab.silmeth.pocztowka

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

fun <T> postcardDecode(bytes: ByteArray, deserializer: DeserializationStrategy<T>): T {
    val decoder = PostcardDecoder(bytes, postcardModule())
    return decoder.decodeSerializableValue(deserializer)
}

inline fun <reified T> postcardDecode(bytes: ByteArray): T {
    val decoder = PostcardDecoder(bytes, postcardModule())
    return decoder.decodeSerializableValue(decoder.serializersModule.serializer())
}

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
class PostcardDecoder(
    input: ByteArray,
    override val serializersModule: SerializersModule,
) : AbstractDecoder() {
    private var input: ByteArrayView = ByteArrayView(input)
    var currentIndex: Int = 0

    override fun decodeByte(): Byte = bite()

    override fun decodeShort(): Short {
        val tmp = decodeLong()
        return if (tmp in Short.MIN_VALUE..Short.MAX_VALUE) {
            tmp.toShort()
        } else {
            throw SerializationException("Value $tmp does not fit in Short")
        }
    }

    fun decodeUShort(): UShort {
        val tmp = decodeULong()
        return if (tmp in UShort.MIN_VALUE..UShort.MAX_VALUE) {
            tmp.toUShort()
        } else {
            throw SerializationException("Value $tmp does not fit in UShort")
        }
    }

    override fun decodeInt(): Int {
        val tmp = decodeLong()
        return if (tmp in Int.MIN_VALUE..Int.MAX_VALUE) {
            tmp.toInt()
        } else {
            throw SerializationException("Value $tmp does not fit in Int")
        }
    }

    fun decodeUInt(): UInt {
        val tmp = decodeULong()
        return if (tmp in UInt.MIN_VALUE..UInt.MAX_VALUE) {
            tmp.toUInt()
        } else {
            throw SerializationException("Value $tmp does not fit in UInt")
        }
    }

    override fun decodeLong(): Long {
        val tmp = decodeULong()
        return unzigzag(tmp.toLong())
    }

    fun decodeULong(): ULong {
        var result: ULong = 0uL
        var b = bite().toInt()

        var shift = 0
        while ((b and 0x80) == 0x80) {
            result += (b and 0x7F).toULong().shl(shift)
            shift += 7
            b = bite().toInt()
        }

        return result + (b and 0xFF).toULong().shl(shift)
    }

    override fun decodeFloat(): Float {
        val b1 = bite().toInt()
        val b2 = bite().toUInt().shl(8).toInt()
        val b3 = bite().toUInt().shl(16).toInt()
        val b4 = bite().toUInt().shl(24).toInt()
        return Float.fromBits(b1 or b2 or b3 or b4)
    }

    override fun decodeDouble(): Double {
        val b1 = bite().toUByte().toLong()
        val b2 = bite().toUByte().toULong().shl(8).toLong()
        val b3 = bite().toUByte().toULong().shl(16).toLong()
        val b4 = bite().toUByte().toULong().shl(24).toLong()
        val b5 = bite().toUByte().toULong().shl(32).toLong()
        val b6 = bite().toUByte().toULong().shl(40).toLong()
        val b7 = bite().toUByte().toULong().shl(48).toLong()
        val b8 = bite().toUByte().toULong().shl(56).toLong()
        return Double.fromBits(b1 or b2 or b3 or b4 or b5 or b6 or b7 or b8)
    }

    override fun decodeString(): String {
        val size = decodeUInt().toInt()
        val result = input.decodeString(size)
        consumeN(size)
        return result
    }

    override fun decodeNotNullMark(): Boolean = decodeBoolean()

    override fun decodeBoolean(): Boolean =
        when (val marker = bite()) {
            0x01.toByte() -> true
            0x00.toByte() -> false
            else -> throw SerializationException("Wrong null-marker $marker")
        }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder =
        if (descriptor.isInline &&
                descriptor.serialName in setOf("kotlin.UShort", "kotlin.UInt", "kotlin.ULong")
        ) {
            UnsignedDecoder(this)
        } else {
            this
        }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (currentIndex == descriptor.elementsCount) {
            CompositeDecoder.DECODE_DONE
        } else {
            currentIndex++
        }

    override fun decodeSequentially(): Boolean = true

    override fun beginStructure(descriptor: SerialDescriptor): PostcardDecoder = this

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = decodeUInt().toInt()

    override fun decodeValue(): Any {
        throw SerializationException("Unsupported type")
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeUInt().toInt()

    private fun unzigzag(value: Long): Long = value.ushr(1) xor (-(value and 0x01))

    private fun bite(): Byte {
        val res = input[0]
        consumeN(1)
        return res
    }

    private fun consumeN(n: Int) {
        input = input.slice(n, input.size)
    }

    class UnsignedDecoder(private val inner: PostcardDecoder) : Decoder by inner {
        override fun decodeShort(): Short = inner.decodeUShort().toShort()
        override fun decodeInt(): Int = inner.decodeUInt().toInt()
        override fun decodeLong(): Long = inner.decodeULong().toLong()
    }
}
