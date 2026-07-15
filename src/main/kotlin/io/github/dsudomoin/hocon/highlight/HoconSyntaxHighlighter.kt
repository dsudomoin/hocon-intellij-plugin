package io.github.dsudomoin.hocon.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import io.github.dsudomoin.hocon.lexer.HoconLexer
import io.github.dsudomoin.hocon.lexer.HoconTokens

class HoconSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = HoconLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = pack(MAP[tokenType])

    private companion object {
        val MAP: Map<IElementType, TextAttributesKey> =
            mapOf(
                HoconTokens.QUOTED_STRING to HoconColors.STRING,
                HoconTokens.MULTILINE_STRING to HoconColors.STRING,
                HoconTokens.HASH_COMMENT to HoconColors.COMMENT,
                HoconTokens.DOUBLE_SLASH_COMMENT to HoconColors.COMMENT,
                HoconTokens.LBRACE to HoconColors.BRACES,
                HoconTokens.RBRACE to HoconColors.BRACES,
                HoconTokens.LBRACKET to HoconColors.BRACKETS,
                HoconTokens.RBRACKET to HoconColors.BRACKETS,
                HoconTokens.LPAREN to HoconColors.PARENTHESES,
                HoconTokens.RPAREN to HoconColors.PARENTHESES,
                HoconTokens.DOLLAR to HoconColors.SUBSTITUTION_SIGN,
                HoconTokens.SUB_LBRACE to HoconColors.BRACES,
                HoconTokens.SUB_RBRACE to HoconColors.BRACES,
                HoconTokens.QMARK to HoconColors.SUBSTITUTION_SIGN,
                HoconTokens.COMMA to HoconColors.COMMA,
                HoconTokens.COLON to HoconColors.SEPARATOR,
                HoconTokens.EQUALS to HoconColors.SEPARATOR,
                HoconTokens.PLUS_EQUALS to HoconColors.SEPARATOR,
                HoconTokens.PERIOD to HoconColors.DOT,
                HoconTokens.BAD_CHARACTER to HoconColors.BAD_CHARACTER,
            )
    }
}

class HoconSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
        HoconSyntaxHighlighter()
}
