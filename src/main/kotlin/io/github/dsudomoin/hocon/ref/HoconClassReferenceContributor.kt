package io.github.dsudomoin.hocon.ref

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.ElementManipulators
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import io.github.dsudomoin.hocon.psi.HStringValue

/**
 * Contributes a reference from a HOCON string value that is a fully-qualified class name to the JVM class,
 * so Ctrl+Click navigates to it. Registered only when the Java plugin is present (see jvm-support.xml).
 */
class HoconClassReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HStringValue::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext,
                ): Array<PsiReference> {
                    val value = element as? HStringValue ?: return PsiReference.EMPTY_ARRAY
                    val manipulator = ElementManipulators.getManipulator(value) ?: return PsiReference.EMPTY_ARRAY
                    val range = manipulator.getRangeInElement(value)
                    val fqcn = range.substring(value.text)
                    if (!FQCN.matches(fqcn)) return PsiReference.EMPTY_ARRAY
                    if (JavaPsiFacade.getInstance(value.project)
                            .findClass(fqcn, GlobalSearchScope.allScope(value.project)) == null
                    ) {
                        return PsiReference.EMPTY_ARRAY
                    }
                    return arrayOf(HoconClassReference(value, range, fqcn))
                }
            },
        )
    }

    private companion object {
        // Dotted identifier with at least two segments (package + class); '$' allowed for nested classes.
        val FQCN = Regex("[\\p{Alpha}_][\\p{Alnum}_]*(\\.[\\p{Alpha}_][\\p{Alnum}_$]*)+")
    }
}
