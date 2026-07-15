package io.github.dsudomoin.hocon.navigation

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.HSubstitutionKey

/** Highlights key declarations as writes and substitution usages as reads. */
class HoconReadWriteAccessDetector : ReadWriteAccessDetector() {
    override fun isReadWriteAccessible(element: PsiElement): Boolean =
        element is HFieldKey || element is HSubstitutionKey

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean = element is HFieldKey

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access = Access.Read

    override fun getExpressionAccess(expression: PsiElement): Access =
        if (expression is HFieldKey) Access.Write else Access.Read
}
