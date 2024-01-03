package com.gitlab.silmeth.pocztowka

actual fun assertFloatsEqual(expected: Float, actual: Float) {
    nonJsAssertFloatsEqual(expected, actual)
}
