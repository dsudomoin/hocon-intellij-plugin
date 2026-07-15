package io.github.dsudomoin.hocon.navigation

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import io.github.dsudomoin.hocon.lexer.HoconLexer
import io.github.dsudomoin.hocon.lexer.HoconTokenSets
import io.github.dsudomoin.hocon.lexer.HoconTokens
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.fullPath
import io.github.dsudomoin.hocon.psi.keyText

class HoconFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner =
        DefaultWordsScanner(
            HoconLexer(),
            TokenSet.create(HoconTokens.UNQUOTED_CHARS),
            HoconTokenSets.COMMENTS,
            HoconTokenSets.STRING_LITERAL,
        )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean = psiElement is HFieldKey

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String = "key"

    override fun getDescriptiveName(element: PsiElement): String = (element as? HFieldKey)?.keyText() ?: ""

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        (element as? HFieldKey)?.let { if (useFullName) it.fullPath().joinToString(".") else it.keyText() } ?: ""
}
