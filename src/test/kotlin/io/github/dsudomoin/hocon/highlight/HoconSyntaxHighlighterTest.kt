package io.github.dsudomoin.hocon.highlight

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.lexer.HoconTokens

class HoconSyntaxHighlighterTest : BasePlatformTestCase() {
    private val highlighter = HoconSyntaxHighlighter()

    fun testQuotedStringUsesStringColor() {
        assertTrue(highlighter.getTokenHighlights(HoconTokens.QUOTED_STRING).contains(HoconColors.STRING))
    }

    fun testHashCommentUsesCommentColor() {
        assertTrue(highlighter.getTokenHighlights(HoconTokens.HASH_COMMENT).contains(HoconColors.COMMENT))
    }

    fun testBraceUsesBraceColor() {
        assertTrue(highlighter.getTokenHighlights(HoconTokens.LBRACE).contains(HoconColors.BRACES))
    }

    fun testSubstitutionBraceUsesBracesColor() {
        assertTrue(highlighter.getTokenHighlights(HoconTokens.SUB_LBRACE).contains(HoconColors.BRACES))
    }

    fun testDollarUsesSubstitutionSignColor() {
        assertTrue(highlighter.getTokenHighlights(HoconTokens.DOLLAR).contains(HoconColors.SUBSTITUTION_SIGN))
    }
}
