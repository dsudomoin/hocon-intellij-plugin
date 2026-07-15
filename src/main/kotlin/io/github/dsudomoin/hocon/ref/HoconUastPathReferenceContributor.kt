package io.github.dsudomoin.hocon.ref

import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.registerUastReferenceProvider
import com.intellij.psi.uastInjectionHostReferenceProvider
import io.github.dsudomoin.hocon.semantics.HoconClasspathConfig
import io.github.dsudomoin.hocon.semantics.HoconPathResolver

/**
 * References from string literals in other JVM languages (Java/Kotlin/Scala, via UAST) to HOCON keys, when
 * the literal's content is a path resolvable in the module's classpath config (`application.conf` /
 * `reference.conf`). Registered only when the Java plugin is present (see jvm-support.xml).
 */
class HoconUastPathReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerUastReferenceProvider(
            injectionHostUExpression(),
            uastInjectionHostReferenceProvider { _, host -> referencesFor(host) },
        )
    }

    private fun referencesFor(host: PsiLanguageInjectionHost): Array<PsiReference> {
        val manipulator = ElementManipulators.getManipulator(host) ?: return PsiReference.EMPTY_ARRAY
        val content = manipulator.getRangeInElement(host).substring(host.text)
        if (!PATH.matches(content)) return PsiReference.EMPTY_ARRAY
        val roots = HoconClasspathConfig.configScope(host)
        if (roots.isEmpty()) return PsiReference.EMPTY_ARRAY
        val path = content.split(".")
        if (HoconPathResolver.resolve(roots, path).isEmpty()) return PsiReference.EMPTY_ARRAY
        return arrayOf(HoconStringLiteralPathReference(host, manipulator.getRangeInElement(host), path))
    }

    private companion object {
        val PATH = Regex("[\\p{Alpha}_][\\p{Alnum}_-]*(\\.[\\p{Alpha}_][\\p{Alnum}_-]*)*")
    }
}
