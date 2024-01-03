package com.gitlab.silmeth.pocztowka

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.Serializable

class PostcardDecoderTest {
    @Test
    fun deserializeSignedInteger() {
        val input = byteArrayOf(0x81.toByte(), 0x01)
        val result = postcardDecode<Int>(input)
        assertEquals(-65, result)
    }

    @Test
    fun shouldDeserializeUnicodeString() {
        val input =
            byteArrayOf(
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
        val expected = "ÿąnczꟁc ÿꟁczi"
        val result = postcardDecode<String>(input)
        assertEquals(expected, result)
    }

    @Test
    fun shouldDeserializeNullableString() {
        val input =
            byteArrayOf(
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
        val expected = "ÿąnczꟁc ÿꟁczi"
        val result = postcardDecode<String?>(input)
        assertEquals(expected, result)
    }

    @Test
    fun shouldDecodeNull() {
        val input = byteArrayOf(0x00)
        val result = postcardDecode<String?>(input)
        assertNull(result)
    }

    @Test
    fun shouldDeserializeUnsigned() {
        val input =
            byteArrayOf(
                0xFF.toByte(),
                0xFF.toByte(),
                0x03,
            )
        val result = postcardDecode<UShort>(input)
        assertEquals(UShort.MAX_VALUE, result)
    }

    @Test
    fun shouldDeserializeSignedCollection() {
        val input =
            byteArrayOf(
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
        val expected =
            listOf(
                0x0001.toShort(), // zigzag: 0x0002
                0x0201.toShort(), // zigzag: 0x0402
                0xFFFF.toShort(), // zigzag: 0x0001
            )
        val result = postcardDecode<List<Short>>(input)
        assertEquals(expected, result)
    }

    @Test
    fun shouldDeserializeUnsignedCollection() {
        val input =
            byteArrayOf(
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
        val expected =
            listOf(
                0x0001u,
                0x0201u,
                UShort.MAX_VALUE,
            )
        val result = postcardDecode<List<UShort>>(input)
        assertEquals(expected, result)
    }

    @Test
    fun shouldDeserializeMap() {
        val input =
            byteArrayOf(
                0x02,
                99,
                98,
                97,
                96,
            )
        val expected = mapOf<UInt, UInt>(99u to 98u, 97u to 96u)
        val result = postcardDecode<Map<UInt, UInt>>(input)
        assertEquals(expected, result)
    }

    @Test
    fun shouldDeserializeFloat() {
        val input =
            byteArrayOf(
                0x00,
                0x06,
                0x00,
                0xc2.toByte(),
            )
        val expected = -32.005859375f
        val result = postcardDecode<Float>(input)

        assertFloatsEqual(expected, result)
    }

    @Test
    fun shouldDeserializeDouble() {
        val input =
            byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x00,
                0xc0.toByte(),
                0x00,
                0x40,
                0xc0.toByte(),
            )
        val expected = -32.005859375
        val result = postcardDecode<Double>(input)
        assertEquals(expected, result)
    }

    @Test
    fun shouldDeserializeDataClass() {
        val input =
            byteArrayOf(
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
        val expected =
            Struct(
                bytes = listOf(0x01, 0x10, 0x02, 0x20),
                str = "hElLo",
            )

        val result = postcardDecode<Struct>(input)

        assertEquals(expected, result)
    }

    @Test
    fun shouldDeserializeEnum() {
        val input =
            byteArrayOf(
                0x04,
                0x00,
                0x01,
                0x02,
                0x03,
            )
        val expected =
            listOf<Enum>(
                Enum.A,
                Enum.B,
                Enum.C,
                Enum.D,
            )

        val result = postcardDecode<List<Enum>>(input)

        assertEquals(expected, result)
    }

    @Serializable data class Struct(val bytes: List<Byte>, val str: String)
    @Serializable
    enum class Enum {
        A,
        B,
        C,
        D,
    }
}
