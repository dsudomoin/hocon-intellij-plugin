package io.github.dsudomoin.hocon.lexer

import com.intellij.psi.tree.IElementType
import io.github.dsudomoin.hocon.lang.HoconLanguage

class HoconTokenType(debugName: String) : IElementType(debugName, HoconLanguage) {
    override fun toString(): String = "HoconToken." + super.toString()
}

object HoconTokens {
    @JvmField
    val INLINE_WHITESPACE = HoconTokenType("INLINE_WHITESPACE")

    @JvmField
    val LINE_BREAKING_WHITESPACE = HoconTokenType("LINE_BREAKING_WHITESPACE")

    @JvmField
    val BAD_CHARACTER = HoconTokenType("BAD_CHARACTER")

    @JvmField
    val LBRACE = HoconTokenType("LBRACE")

    @JvmField
    val RBRACE = HoconTokenType("RBRACE")

    @JvmField
    val LBRACKET = HoconTokenType("LBRACKET")

    @JvmField
    val RBRACKET = HoconTokenType("RBRACKET")

    @JvmField
    val LPAREN = HoconTokenType("LPAREN")

    @JvmField
    val RPAREN = HoconTokenType("RPAREN")

    @JvmField
    val COLON = HoconTokenType("COLON")

    @JvmField
    val COMMA = HoconTokenType("COMMA")

    @JvmField
    val EQUALS = HoconTokenType("EQUALS")

    @JvmField
    val PLUS_EQUALS = HoconTokenType("PLUS_EQUALS")

    @JvmField
    val PERIOD = HoconTokenType("PERIOD")

    @JvmField
    val DOLLAR = HoconTokenType("DOLLAR")

    @JvmField
    val SUB_LBRACE = HoconTokenType("SUB_LBRACE")

    @JvmField
    val QMARK = HoconTokenType("QMARK")

    @JvmField
    val SUB_RBRACE = HoconTokenType("SUB_RBRACE")

    @JvmField
    val HASH_COMMENT = HoconTokenType("HASH_COMMENT")

    @JvmField
    val DOUBLE_SLASH_COMMENT = HoconTokenType("DOUBLE_SLASH_COMMENT")

    @JvmField
    val UNQUOTED_CHARS = HoconTokenType("UNQUOTED_CHARS")

    @JvmField
    val QUOTED_STRING = HoconTokenType("QUOTED_STRING")

    @JvmField
    val MULTILINE_STRING = HoconTokenType("MULTILINE_STRING")
}
