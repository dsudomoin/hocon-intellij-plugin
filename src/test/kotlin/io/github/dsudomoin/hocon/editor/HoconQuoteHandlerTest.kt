package io.github.dsudomoin.hocon.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconQuoteHandlerTest : BasePlatformTestCase() {
    fun testTypingQuoteAutoInsertsClosing() {
        myFixture.configureByText("a.conf", "a = <caret>")
        myFixture.type('"')
        assertEquals("a = " + '"' + '"', myFixture.editor.document.text)
    }
}
