package io.github.dsudomoin.hocon.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import io.github.dsudomoin.hocon.psi.keyText
import io.github.dsudomoin.hocon.semantics.HoconClasspathConfig
import io.github.dsudomoin.hocon.semantics.HoconPathResolver

/**
 * Completes HOCON paths inside string literals of other JVM languages (Java/Kotlin) against the module's
 * classpath config: typing `"app.<caret>"` offers the children of `app`. Registered via jvm-support.xml, so
 * it is active only when the Java plugin is present. Offers nothing when the typed prefix does not resolve
 * to a config object, so it stays quiet in unrelated string literals.
 */
class HoconForeignLiteralCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    val host =
                        PsiTreeUtil.getParentOfType(parameters.position, PsiLanguageInjectionHost::class.java) ?: return
                    if (!host.isValidHost) return
                    val contentRange = ElementManipulators.getManipulator(host)?.getRangeInElement(host) ?: return
                    val caretInHost = parameters.offset - host.textRange.startOffset
                    if (caretInHost < contentRange.startOffset || caretInHost > contentRange.endOffset) return

                    val typed = host.text.substring(contentRange.startOffset, caretInHost)
                    if (!PATH_PREFIX.matches(typed)) return

                    val roots = HoconClasspathConfig.configScope(parameters.originalFile)
                    if (roots.isEmpty()) return

                    val segments = typed.split(".")
                    val children =
                        HoconPathResolver.childScope(roots, segments.dropLast(1))
                            .mapNotNull { it.key()?.keyText() }
                            .distinct()
                    if (children.isEmpty()) return

                    val prefixed = result.withPrefixMatcher(segments.last())
                    for (key in children) {
                        prefixed.addElement(LookupElementBuilder.create(key).withIcon(AllIcons.Nodes.Property))
                    }
                }
            },
        )
    }

    private companion object {
        /** Dotted identifier segments typed so far, optionally ending with a dot (empty trailing segment). */
        val PATH_PREFIX = Regex("([\\p{Alpha}_][\\p{Alnum}_-]*(\\.[\\p{Alpha}_][\\p{Alnum}_-]*)*\\.?)?")
    }
}
