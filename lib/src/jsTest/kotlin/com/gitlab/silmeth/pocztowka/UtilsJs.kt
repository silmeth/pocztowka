package com.gitlab.silmeth.pocztowka

import kotlin.test.assertTrue

// in JS Floats created from bits lose precision
actual fun assertFloatsEqual(expected: Float, actual: Float) {
    assertTrue(
        expected - 0.00001 <= actual && actual <= expected + 0.00001,
        "Expected: $expected, got: $actual",
    )
}
