package io.github.dsudomoin.hocon.lexer

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import io.github.dsudomoin.hocon.lexer.HoconTokens.BAD_CHARACTER
import io.github.dsudomoin.hocon.lexer.HoconTokens.COLON
import io.github.dsudomoin.hocon.lexer.HoconTokens.COMMA
import io.github.dsudomoin.hocon.lexer.HoconTokens.DOLLAR
import io.github.dsudomoin.hocon.lexer.HoconTokens.DOUBLE_SLASH_COMMENT
import io.github.dsudomoin.hocon.lexer.HoconTokens.EQUALS
import io.github.dsudomoin.hocon.lexer.HoconTokens.HASH_COMMENT
import io.github.dsudomoin.hocon.lexer.HoconTokens.INLINE_WHITESPACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.LBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.LBRACKET
import io.github.dsudomoin.hocon.lexer.HoconTokens.LINE_BREAKING_WHITESPACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.LPAREN
import io.github.dsudomoin.hocon.lexer.HoconTokens.MULTILINE_STRING
import io.github.dsudomoin.hocon.lexer.HoconTokens.PERIOD
import io.github.dsudomoin.hocon.lexer.HoconTokens.PLUS_EQUALS
import io.github.dsudomoin.hocon.lexer.HoconTokens.QUOTED_STRING
import io.github.dsudomoin.hocon.lexer.HoconTokens.RBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.RBRACKET
import io.github.dsudomoin.hocon.lexer.HoconTokens.RPAREN
import io.github.dsudomoin.hocon.lexer.HoconTokens.SUB_RBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.UNQUOTED_CHARS

object HoconTokenSets {
    val WHITESPACE: TokenSet = TokenSet.create(INLINE_WHITESPACE, LINE_BREAKING_WHITESPACE)
    val COMMENTS: TokenSet = TokenSet.create(HASH_COMMENT, DOUBLE_SLASH_COMMENT)
    val STRING_LITERAL: TokenSet = TokenSet.create(QUOTED_STRING, MULTILINE_STRING)
    val KEY_VALUE_SEPARATOR: TokenSet = TokenSet.create(COLON, EQUALS, PLUS_EQUALS)
    val ARRAY_ELEMENTS_ENDING: TokenSet = TokenSet.create(RBRACKET, RBRACE)
    val VALUE_ENDING: TokenSet = TokenSet.create(COMMA, RBRACE, RBRACKET)
    val UNQUOTED_OR_PARENS: TokenSet = TokenSet.create(UNQUOTED_CHARS, LPAREN, RPAREN)
    val VALUE_UNQUOTED_CHARS: TokenSet = TokenSet.orSet(UNQUOTED_OR_PARENS, TokenSet.create(PERIOD))
    val PATH_ENDING: TokenSet =
        TokenSet.orSet(KEY_VALUE_SEPARATOR, TokenSet.create(LBRACE, SUB_RBRACE), VALUE_ENDING)
    val KEY_ENDING: TokenSet = TokenSet.orSet(PATH_ENDING, TokenSet.create(PERIOD))
    val SIMPLE_VALUE_PART: TokenSet =
        TokenSet.orSet(UNQUOTED_OR_PARENS, TokenSet.create(PERIOD), STRING_LITERAL)
    val PATH_START: TokenSet =
        TokenSet.orSet(UNQUOTED_OR_PARENS, STRING_LITERAL, TokenSet.create(PERIOD, DOLLAR, BAD_CHARACTER))
    val SUBSTITUTION_PATH_START: TokenSet = TokenSet.orSet(PATH_START, KEY_VALUE_SEPARATOR)
    val VALUE_START: TokenSet =
        TokenSet.orSet(
            SIMPLE_VALUE_PART,
            TokenSet.create(LBRACE, LBRACKET, DOLLAR, BAD_CHARACTER),
            KEY_VALUE_SEPARATOR,
        )
    val OBJECT_ENTRY_START: TokenSet = PATH_START
    val EMPTY: TokenSet = TokenSet.EMPTY

    // Extra HOCON whitespace beyond Char.isWhitespace(), matched by codepoint:
    // 0x00A0 NBSP, 0x2007 FIGURE SPACE, 0x202F NARROW NO-BREAK SPACE, 0xFEFF ZERO WIDTH NO-BREAK SPACE (BOM)
    fun isHoconWhitespace(c: Char): Boolean =
        c.isWhitespace() || c.code == 0x00A0 || c.code == 0x2007 || c.code == 0x202F || c.code == 0xFEFF
}

/** Токен-матчер с учётом переводов строки и конца файла. */
class Matcher(
    val tokens: TokenSet,
    val requireNoNewLine: Boolean = false,
    val matchNewLine: Boolean = false,
    val matchEof: Boolean = false,
) {
    fun noNewLine(): Matcher = Matcher(tokens, requireNoNewLine = true, matchNewLine, matchEof)

    fun orNewLineOrEof(): Matcher = Matcher(tokens, requireNoNewLine, matchNewLine = true, matchEof = true)

    fun orEof(): Matcher = Matcher(tokens, requireNoNewLine, matchNewLine, matchEof = true)
}

fun IElementType.m(): Matcher = Matcher(TokenSet.create(this))

fun TokenSet.m(): Matcher = Matcher(this)
