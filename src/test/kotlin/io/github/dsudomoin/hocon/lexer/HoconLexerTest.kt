package io.github.dsudomoin.hocon.lexer

import com.intellij.psi.tree.IElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HoconLexerTest {
    private fun lex(text: String): List<Pair<IElementType, String>> {
        val lexer = HoconLexer()
        lexer.start(text, 0, text.length, 0)
        val out = ArrayList<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            out.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return out
    }

    private fun types(text: String): List<IElementType> = lex(text).map { it.first }

    @Test
    fun simpleField() {
        assertEquals(
            listOf(
                HoconTokens.UNQUOTED_CHARS to "a",
                HoconTokens.INLINE_WHITESPACE to " ",
                HoconTokens.EQUALS to "=",
                HoconTokens.INLINE_WHITESPACE to " ",
                HoconTokens.UNQUOTED_CHARS to "b",
            ),
            lex("a = b"),
        )
    }

    @Test
    fun optionalSubstitution() {
        val input = "$" + "{?a.b}"
        assertEquals(
            listOf(
                HoconTokens.DOLLAR,
                HoconTokens.SUB_LBRACE,
                HoconTokens.QMARK,
                HoconTokens.UNQUOTED_CHARS,
                HoconTokens.PERIOD,
                HoconTokens.UNQUOTED_CHARS,
                HoconTokens.SUB_RBRACE,
            ),
            types(input),
        )
    }

    @Test
    fun objectBracesAreObjectTokens() {
        assertEquals(listOf(HoconTokens.LBRACE, HoconTokens.RBRACE), types("{}"))
    }

    @Test
    fun brackets() {
        assertEquals(listOf(HoconTokens.LBRACKET, HoconTokens.RBRACKET), types("[]"))
    }

    @Test
    fun comments() {
        assertEquals(HoconTokens.HASH_COMMENT, types("# c").first())
        assertEquals(HoconTokens.DOUBLE_SLASH_COMMENT, types("// c").first())
    }

    @Test
    fun newlineIsLineBreaking() {
        assertEquals(HoconTokens.LINE_BREAKING_WHITESPACE, types("a\nb")[1])
    }

    @Test
    fun quotedString() {
        val input = "" + '"' + "x" + '"'
        val first = lex(input).first()
        assertEquals(HoconTokens.QUOTED_STRING, first.first)
        assertEquals(input, first.second)
    }

    @Test
    fun multilineString() {
        val q3 = "" + '"' + '"' + '"'
        val input = q3 + "x" + q3
        val first = lex(input).first()
        assertEquals(HoconTokens.MULTILINE_STRING, first.first)
        assertEquals(input, first.second)
    }

    @Test
    fun plusEquals() {
        assertTrue(types("a += b").contains(HoconTokens.PLUS_EQUALS))
    }
}
