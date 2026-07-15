package io.github.dsudomoin.hocon.navigation

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.fullPath
import io.github.dsudomoin.hocon.semantics.HoconPathResolver
import io.github.dsudomoin.hocon.semantics.HoconResolution

/**
 * Renames a HOCON key across ALL its declarations (a key may be declared many times under merge semantics).
 * Substitution usages are renamed automatically through their references; here we only add the sibling
 * declarations of the same path so the platform renames them together with the primary element.
 */
class HoconRenameProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean = element is HFieldKey

    override fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
        val key = element as? HFieldKey ?: return
        val file = key.containingFile ?: return
        val path = key.fullPath()
        if (path.isEmpty()) return
        for (declaration in HoconPathResolver.resolve(HoconResolution.toplevelScope(file), path)) {
            if (declaration != key) allRenames[declaration] = newName
        }
    }
}
