package io.github.dsudomoin.hocon.semantics

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.lang.HoconFile
import io.github.dsudomoin.hocon.psi.HObjectEntries
import io.github.dsudomoin.hocon.psi.keyText

class HoconPathResolverTest : BasePlatformTestCase() {
    private fun roots(text: String): List<HObjectEntries> {
        val f = myFixture.configureByText("t.conf", text) as HoconFile
        return listOfNotNull(HoconResolution.rootEntries(f))
    }

    private fun resolveTexts(text: String, vararg path: String): List<String> =
        HoconPathResolver.resolve(roots(text), path.toList()).map { it.keyText() }

    fun testSimpleKey() = assertEquals(listOf("a"), resolveTexts("a = 1", "a"))

    fun testNestedObject() = assertEquals(listOf("host"), resolveTexts("s { host = x }", "s", "host"))

    fun testPrefixedPath() = assertEquals(listOf("c"), resolveTexts("a.b.c = 1", "a", "b", "c"))

    fun testMergeCollectsAllDeclarations() {
        val r = HoconPathResolver.resolve(roots("a { x = 1 }\na { y = 2 }"), listOf("a"))
        assertEquals(2, r.size)
    }

    fun testObjectConcatenationMerge() =
        assertEquals(listOf("y"), resolveTexts("a = { x = 1 } { y = 2 }", "a", "y"))

    fun testDescendPrefixedThenObject() =
        assertEquals(listOf("port"), resolveTexts("a.b { port = 1 }", "a", "b", "port"))

    fun testUnresolvedReturnsEmpty() = assertEquals(emptyList<String>(), resolveTexts("a = 1", "b"))
}
