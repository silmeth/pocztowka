package com.gitlab.silmeth.pocztowka

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable

class PostcardEncoderTest {
    @Test
    fun writeSomethingToBuffer() {
        val encoder = PostcardEncoder(serializersModule = postcardModule())
        for (i in 0 until 53) {
            encoder.encodeByte(i.toByte())
        }

        assertTrue(encoder.toBytes().size == 53)
    }

    @Test
    fun serializeSignedInteger() {
        val result = postcardEncode(-65)
        assertEquals(listOf<Byte>(0x81.toByte(), 0x01), result.toList())
    }

    @Test
    fun shouldSerializeUnicodeString() {
        val result = postcardEncode("ÿąnczꟁc ÿꟁczi")
        val expected =
            listOf<Byte>(
                20,
                195.toByte(),
                191.toByte(),
                196.toByte(),
                133.toByte(),
                110,
                99,
                122,
                234.toByte(),
                159.toByte(),
                129.toByte(),
                99,
                32,
                195.toByte(),
                191.toByte(),
                234.toByte(),
                159.toByte(),
                129.toByte(),
                99,
                122,
                105,
            )
        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeNullableString() {
        val str: String? = "ÿąnczꟁc ÿꟁczi"
        val result = postcardEncode(str)
        val expected =
            listOf<Byte>(
                1,
                20,
                195.toByte(),
                191.toByte(),
                196.toByte(),
                133.toByte(),
                110,
                99,
                122,
                234.toByte(),
                159.toByte(),
                129.toByte(),
                99,
                32,
                195.toByte(),
                191.toByte(),
                234.toByte(),
                159.toByte(),
                129.toByte(),
                99,
                122,
                105,
            )
        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeUnsigned() {
        val result = postcardEncode(UShort.MAX_VALUE)
        val expected =
            listOf<Byte>(
                0xFF.toByte(),
                0xFF.toByte(),
                0x03,
            )
        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeSignedCollection() {
        val result =
            postcardEncode(
                listOf(
                    0x0001.toShort(), // zigzag: 0x0002
                    0x0201.toShort(), // zigzag: 0x0402
                    0xFFFF.toShort(), // zigzag: 0x0001
                )
            )
        val expected =
            listOf<Byte>(
                // num of elems
                0x03,
                // 1. number
                0x02,
                // 2. number, LSB
                0x82.toByte(),
                // 2. MSB
                0x08,
                // 3. number
                0x01,
            )
        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeUnsignedCollection() {
        val result =
            postcardEncode(
                listOf(
                    0x0001u,
                    0x0201u,
                    UShort.MAX_VALUE,
                )
            )
        val expected =
            listOf<Byte>(
                // num of elems
                0x03,
                // 1. number
                0x01,
                // 2. number, LSB
                0x81.toByte(),
                // 2. MSB
                0x04,
                // 3. number
                0xFF.toByte(),
                0xFF.toByte(),
                0x03,
            )
        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeFloat() {
        val result = postcardEncode(-32.005859375f)
        val expected =
            listOf<Byte>(
                0x00,
                0x06,
                0x00,
                0xc2.toByte(),
            )
        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeDouble() {
        val result = postcardEncode(-32.005859375)
        val expected =
            listOf<Byte>(
                0x00,
                0x00,
                0x00,
                0x00,
                0xc0.toByte(),
                0x00,
                0x40,
                0xc0.toByte(),
            )
        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeMap() {
        val result = postcardEncode(mapOf<UInt, UInt>(99u to 98u, 97u to 96u))
        val expected =
            listOf<Byte>(
                0x02,
                99,
                98,
                97,
                96,
            )
        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeDataClass() {
        val result =
            postcardEncode(
                Struct(
                    bytes = byteArrayOf(0x01, 0x10, 0x02, 0x20),
                    str = "hElLo",
                )
            )
        val expected =
            listOf<Byte>(
                0x04,
                0x01,
                0x10,
                0x02,
                0x20,
                0x05,
                'h'.code.toByte(),
                'E'.code.toByte(),
                'l'.code.toByte(),
                'L'.code.toByte(),
                'o'.code.toByte(),
            )

        assertEquals(expected, result.toList())
    }

    @Test
    fun shouldSerializeEnum() {
        val result =
            postcardEncode(
                listOf<Enum>(
                    Enum.A,
                    Enum.B,
                    Enum.C,
                    Enum.D,
                )
            )
        val expected =
            listOf<Byte>(
                0x04,
                0x00,
                0x01,
                0x02,
                0x03,
            )

        assertEquals(expected, result.toList())
    }

    // @Test
    // fun shouldSerializePolymorphic() {
    //     val result = postcardEncode(FooBar.Bar(0x69.toUInt()) as FooBar)
    //     println(result.toList().map { it.toUInt().toString(16) })
    //     assertEquals(1, 2)
    // }

    @Serializable data class Struct(val bytes: ByteArray, val str: String)
    @Serializable
    enum class Enum {
        A,
        B,
        C,
        D,
    }

    // @Serializable
    // sealed class FooBar {
    //     @Serializable data class Foo(val foo: String) : FooBar()
    //     @Serializable data class Bar(val bar: UInt) : FooBar()
    // }
}
