package io.github.dsudomoin.hocon.structure

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import io.github.dsudomoin.hocon.lang.HoconLanguage
import io.github.dsudomoin.hocon.psi.HObjectField
import io.github.dsudomoin.hocon.psi.presentableKey

class HoconBreadcrumbsProvider : BreadcrumbsProvider {
    override fun getLanguages(): Array<Language> = arrayOf(HoconLanguage)

    override fun acceptElement(element: PsiElement): Boolean = element is HObjectField

    override fun getElementInfo(element: PsiElement): String = (element as HObjectField).presentableKey()
}
