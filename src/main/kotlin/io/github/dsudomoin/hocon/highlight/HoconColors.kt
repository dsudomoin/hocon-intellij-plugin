package io.github.dsudomoin.hocon.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object HoconColors {
    val KEY = key("HOCON_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val STRING = key("HOCON_STRING", DefaultLanguageHighlighterColors.STRING)
    val NUMBER = key("HOCON_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val KEYWORD = key("HOCON_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val SUBSTITUTION = key("HOCON_SUBSTITUTION", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val SUBSTITUTION_SIGN = key("HOCON_SUBSTITUTION_SIGN", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val BRACES = key("HOCON_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val BRACKETS = key("HOCON_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val PARENTHESES = key("HOCON_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val COMMA = key("HOCON_COMMA", DefaultLanguageHighlighterColors.COMMA)
    val SEPARATOR = key("HOCON_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val DOT = key("HOCON_DOT", DefaultLanguageHighlighterColors.DOT)
    val COMMENT = key("HOCON_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val BAD_CHARACTER = key("HOCON_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

    private fun key(externalName: String, fallback: TextAttributesKey): TextAttributesKey =
        TextAttributesKey.createTextAttributesKey(externalName, fallback)
}
