package com.gitlab.silmeth.pocztowka

internal class ByteArrayView(private val array: ByteArray, private val startIdx: Int = 0, private val endIdx: Int = array.size) {
    fun decodeString(size: Int): String = array.decodeToString(startIdx, startIdx + size)
    operator fun get(idx: Int): Byte = array[startIdx + idx]
    fun slice(from: Int, to: Int) = ByteArrayView(array, startIdx + from, startIdx + to)
    val size: Int get() = endIdx - startIdx
}
