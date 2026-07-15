package io.github.dsudomoin.hocon.ref

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import io.github.dsudomoin.hocon.semantics.HoconClasspathConfig
import io.github.dsudomoin.hocon.semantics.HoconPathResolver

/**
 * A reference from a HOCON path written in another language's string literal (e.g. a `@Value("app.name")`)
 * to that key's declarations in the module's classpath config. Soft, so path-shaped strings that are not
 * config keys are not reported as unresolved.
 */
class HoconStringLiteralPathReference(
    element: PsiElement,
    range: TextRange,
    private val path: List<String>,
) : PsiPolyVariantReferenceBase<PsiElement>(element, range, true) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> =
        HoconPathResolver.resolve(HoconClasspathConfig.configScope(element), path)
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
}
