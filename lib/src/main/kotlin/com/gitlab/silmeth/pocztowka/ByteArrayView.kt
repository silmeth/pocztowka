package com.gitlab.silmeth.pocztowka

internal class ByteArrayView(
    private val array: ByteArray,
    private val startIdx: Int = 0,
    private val endIdx: Int = array.size,
) {
    init {
        require(startIdx >= 0 && endIdx <= array.size)
    }

    fun decodeString(length: Int): String = if (length <= this.size) {
        array.decodeToString(startIdx, startIdx + length)
    } else {
        throw IndexOutOfBoundsException()
    }

    operator fun get(idx: Int): Byte {
        val targetIdx = startIdx + idx
        return if (idx >= 0 && targetIdx < endIdx) {
            array[targetIdx]
        } else {
            throw IndexOutOfBoundsException()
        }
    }

    fun slice(from: Int, to: Int): ByteArrayView {
        val targetFrom = startIdx + from
        val targetTo = startIdx + to

        return if (from <= to && targetFrom >= 0 && targetTo <= endIdx) {
            ByteArrayView(array, targetFrom, targetTo)
        } else {
            throw IndexOutOfBoundsException()
        }
    }

    val size: Int get() = endIdx - startIdx
}
