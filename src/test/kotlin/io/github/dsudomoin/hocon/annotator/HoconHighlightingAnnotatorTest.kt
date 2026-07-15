package io.github.dsudomoin.hocon.annotator

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.highlight.HoconColors
import io.github.dsudomoin.hocon.highlight.HoconValueHighlighter

class HoconHighlightingAnnotatorTest : BasePlatformTestCase() {
    /** Colors the value `FAST` of the top-level key `mode`, mimicking an external schema enum highlighter. */
    private class FakeValueHighlighter : HoconValueHighlighter {
        override fun valueHighlight(file: PsiFile, keyPath: List<String>, valueText: String): TextAttributesKey? =
            if (keyPath == listOf("mode") && valueText == "FAST") ENUM_KEY else null
    }

    override fun setUp() {
        super.setUp()
        HoconValueHighlighter.EP_NAME.point.registerExtension(FakeValueHighlighter(), testRootDisposable)
    }

    fun testKeyPartGetsKeyAttributes() {
        myFixture.configureByText("a.conf", "server.host = localhost")
        val infos = myFixture.doHighlighting()
        assertTrue(infos.any { it.forcedTextAttributesKey == HoconColors.KEY })
    }

    fun testSubstitutionKeyGetsSubstitutionAttributes() {
        myFixture.configureByText("a.conf", "x = " + "$" + "{a.b}")
        val infos = myFixture.doHighlighting()
        assertTrue(infos.any { it.forcedTextAttributesKey == HoconColors.SUBSTITUTION })
    }

    fun testNumberGetsNumberAttributes() {
        myFixture.configureByText("a.conf", "port = 8080")
        assertTrue(myFixture.doHighlighting().any { it.forcedTextAttributesKey == HoconColors.NUMBER })
    }

    fun testBooleanGetsKeywordAttributes() {
        myFixture.configureByText("a.conf", "enabled = true")
        assertTrue(myFixture.doHighlighting().any { it.forcedTextAttributesKey == HoconColors.KEYWORD })
    }

    fun testNullGetsKeywordAttributes() {
        myFixture.configureByText("a.conf", "x = null")
        assertTrue(myFixture.doHighlighting().any { it.forcedTextAttributesKey == HoconColors.KEYWORD })
    }

    fun testIncludeKeywordGetsKeywordAttributes() {
        myFixture.configureByText("a.conf", "include \"x.conf\"")
        assertTrue(myFixture.doHighlighting().any { it.forcedTextAttributesKey == HoconColors.KEYWORD })
    }

    fun testEnumValueGetsHighlighterAttributes() {
        myFixture.configureByText("a.conf", "mode = FAST")
        assertTrue(myFixture.doHighlighting().any { it.forcedTextAttributesKey == ENUM_KEY })
    }

    fun testQuotedEnumValueGetsHighlighterAttributes() {
        myFixture.configureByText("a.conf", "mode = \"FAST\"")
        assertTrue(myFixture.doHighlighting().any { it.forcedTextAttributesKey == ENUM_KEY })
    }

    fun testNonMatchingValueGetsNoHighlighterAttributes() {
        myFixture.configureByText("a.conf", "mode = SLOW")
        assertFalse(myFixture.doHighlighting().any { it.forcedTextAttributesKey == ENUM_KEY })
    }

    fun testValueOfUnknownKeyGetsNoHighlighterAttributes() {
        myFixture.configureByText("a.conf", "other = FAST")
        assertFalse(myFixture.doHighlighting().any { it.forcedTextAttributesKey == ENUM_KEY })
    }

    companion object {
        private val ENUM_KEY: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("TEST_HOCON_ENUM", DefaultLanguageHighlighterColors.CONSTANT)
    }
}
