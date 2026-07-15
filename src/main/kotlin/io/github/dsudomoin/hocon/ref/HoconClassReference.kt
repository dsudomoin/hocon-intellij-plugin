package io.github.dsudomoin.hocon.ref

import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope

/** A reference from a fully-qualified class name in a HOCON string value to the JVM class it names. */
class HoconClassReference(element: PsiElement, range: TextRange, private val fqcn: String) :
    PsiReferenceBase<PsiElement>(element, range, true) {
    override fun resolve(): PsiElement? =
        JavaPsiFacade.getInstance(element.project).findClass(fqcn, GlobalSearchScope.allScope(element.project))
}
