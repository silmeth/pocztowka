package com.gitlab.silmeth.pocztowka

internal class ByteArrayView(
    private val array: ByteArray,
    private val startIdx: Int = 0,
    private val endIdx: Int = array.size,
) {
    init {
        require(startIdx >= 0 && endIdx <= array.size)
    }

    fun decodeString(length: Int): String =
        if (length <= this.size) {
            array.decodeToString(startIdx, startIdx + length, throwOnInvalidSequence = true)
        } else {
            throw IndexOutOfBoundsException("Length $length outside of array size ${this.size}")
        }

    operator fun get(idx: Int): Byte {
        val targetIdx = startIdx + idx
        return if (idx >= 0 && targetIdx < endIdx) {
            array[targetIdx]
        } else {
            throw IndexOutOfBoundsException("Index $idx outside of array size ${this.size}")
        }
    }

    fun slice(from: Int, to: Int): ByteArrayView {
        val targetFrom = startIdx + from
        val targetTo = startIdx + to

        return if (from <= to && targetFrom >= 0 && targetTo <= endIdx) {
            ByteArrayView(array, targetFrom, targetTo)
        } else {
            throw IndexOutOfBoundsException(
                "Indices [$from, $to) outside of array size ${this.size}"
            )
        }
    }

    val size: Int
        get() = endIdx - startIdx
}
