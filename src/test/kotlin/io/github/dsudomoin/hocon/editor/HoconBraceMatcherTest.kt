package io.github.dsudomoin.hocon.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class HoconBraceMatcherTest {
    @Test
    fun hasThreeBracePairs() {
        assertEquals(3, HoconBraceMatcher().pairs.size)
    }
}
