package io.github.dsudomoin.hocon.structure

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconStructureViewTest : BasePlatformTestCase() {
    fun testTopLevelAndNestedKeys() {
        myFixture.configureByText("a.conf", "a {\n  b = 1\n}")
        val root = HoconStructureViewElement(myFixture.file)

        val top = root.children
        assertEquals(1, top.size)
        assertEquals("a", top[0].presentation.presentableText)

        val nested = top[0].children
        assertEquals(1, nested.size)
        assertEquals("b", nested[0].presentation.presentableText)
    }
}
