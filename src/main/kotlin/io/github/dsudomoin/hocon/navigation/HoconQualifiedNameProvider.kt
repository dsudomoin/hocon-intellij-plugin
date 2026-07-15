package io.github.dsudomoin.hocon.navigation

import com.intellij.ide.actions.QualifiedNameProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.psi.*

/** "Copy Reference" — yields the full dotted path of a key or substitution segment. */
class HoconQualifiedNameProvider : QualifiedNameProvider {
    override fun adjustElementToCopy(element: PsiElement): PsiElement? = null

    override fun getQualifiedName(element: PsiElement): String? =
        when (element) {
            is HFieldKey -> element.fullPath().joinToString(".")
            is HSubstitutionKey -> substitutionPrefix(element)
            else -> null
        }

    override fun qualifiedNameToElement(fqn: String, project: Project): PsiElement? = null

    private fun substitutionPrefix(key: HSubstitutionKey): String? {
        val keys = PsiTreeUtil.getParentOfType(key, HSubstitution::class.java)?.path()?.allKeys() ?: return null
        val idx = keys.indexOf(key)
        if (idx < 0) return null
        return keys.subList(0, idx + 1).joinToString(".") { it.keyText() }
    }
}
