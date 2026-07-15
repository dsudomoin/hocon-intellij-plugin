package io.github.dsudomoin.hocon.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import io.github.dsudomoin.hocon.lang.HoconFile
import io.github.dsudomoin.hocon.lexer.HoconLexer
import io.github.dsudomoin.hocon.lexer.HoconTokenSets
import io.github.dsudomoin.hocon.lexer.HoconTokens
import io.github.dsudomoin.hocon.psi.HoconPsiFactory

class HoconParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = HoconLexer()

    override fun createParser(project: Project?): PsiParser = HoconPsiParser()

    override fun getFileNodeType(): IFileElementType = HoconElements.FILE

    override fun getCommentTokens(): TokenSet = HoconTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = HoconTokenSets.STRING_LITERAL

    override fun getWhitespaceTokens(): TokenSet = HoconTokenSets.WHITESPACE

    override fun createElement(node: ASTNode): PsiElement = HoconPsiFactory.create(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = HoconFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode): SpaceRequirements {
        val l = left.elementType
        val r = right.elementType
        val forbidden =
            (l == HoconTokens.DOLLAR && r == HoconTokens.SUB_LBRACE) ||
                    (l == HoconTokens.SUB_LBRACE && r == HoconTokens.QMARK)
        return if (forbidden) SpaceRequirements.MUST_NOT else SpaceRequirements.MAY
    }
}
