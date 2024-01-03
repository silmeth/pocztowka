package com.gitlab.silmeth.pocztowka

import kotlin.test.assertEquals

// in JS Floats created from bits lose precision
expect fun assertFloatsEqual(expected: Float, actual: Float)

fun nonJsAssertFloatsEqual(expected: Float, actual: Float) {
    assertEquals(expected, actual)
}

