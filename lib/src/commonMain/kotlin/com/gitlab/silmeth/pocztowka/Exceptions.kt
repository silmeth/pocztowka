package com.gitlab.silmeth.pocztowka

import kotlinx.serialization.SerializationException as KtxSerializationException

sealed class DeserializationException : KtxSerializationException() {
    class OutOfBounds(val value: Long, val type: String, unsigned: Boolean = false) : DeserializationException() {
        override val message: String = if (unsigned) {
            "Value ${value.toULong()} out of bounds for $type"
        } else {
            "Value $value out of bounds for $type"
        }
    }
    class UnexpectedValue(val expected: String, val actual: Byte) : DeserializationException() {
        override val message: String = "Expected $expected, got ${actual.toUInt().toString(16)}"
    }
    class UnsupportedType : DeserializationException() {
        override val message: String = "Unsupported type for deserialization target"
    }
}

sealed class SerializationException : KtxSerializationException() {
    class UnsupportedType : DeserializationException() {
        override val message: String = "Unsupported type for serialization target"
    }
}
