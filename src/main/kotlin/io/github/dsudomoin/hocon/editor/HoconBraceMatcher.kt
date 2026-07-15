package io.github.dsudomoin.hocon.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import io.github.dsudomoin.hocon.lexer.HoconTokens

class HoconBraceMatcher : PairedBraceMatcher {
    private val pairs =
        arrayOf(
            BracePair(HoconTokens.LBRACE, HoconTokens.RBRACE, true),
            BracePair(HoconTokens.LBRACKET, HoconTokens.RBRACKET, false),
            BracePair(HoconTokens.SUB_LBRACE, HoconTokens.SUB_RBRACE, false),
        )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}
