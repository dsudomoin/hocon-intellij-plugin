package io.github.dsudomoin.hocon.ref

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.ResolveCache
import io.github.dsudomoin.hocon.psi.HSubstitutionKey
import io.github.dsudomoin.hocon.semantics.HoconResolution

/**
 * Reference from a substitution key to the declarations of its path prefix.
 * `${a.b.c}` — the reference on `b` resolves to every declaration of `a.b` (poly, honoring merge/duplication).
 */
class HKeyReference(key: HSubstitutionKey) :
    PsiPolyVariantReferenceBase<HSubstitutionKey>(key, TextRange(0, key.textLength)) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        ResolveCache.getInstance(element.project).resolveWithCaching(this, RESOLVER, false, incompleteCode)

    override fun handleElementRename(newElementName: String): PsiElement {
        val manipulator = ElementManipulators.getManipulator(element) ?: return element
        return manipulator.handleContentChange(element, newElementName) ?: element
    }

    private companion object {
        val RESOLVER =
            ResolveCache.PolyVariantResolver<HKeyReference> { ref, _ ->
                val results: List<ResolveResult> =
                    HoconResolution.resolveSubstitutionKey(ref.element).map { PsiElementResolveResult(it) }
                results.toTypedArray()
            }
    }
}
