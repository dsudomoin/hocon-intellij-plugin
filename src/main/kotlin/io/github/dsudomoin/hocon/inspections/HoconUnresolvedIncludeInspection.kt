package io.github.dsudomoin.hocon.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import io.github.dsudomoin.hocon.psi.HInclude
import io.github.dsudomoin.hocon.semantics.HoconIncludeResolver

/**
 * Flags an `include "file"` / `include file("...")` whose target file cannot be found. `classpath(...)` and
 * `url(...)` includes are never flagged (they resolve dynamically). A `required(...)` miss is an error.
 */
class HoconUnresolvedIncludeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is HInclude) return
                val included = element.included() ?: return
                val qualified = included.qualifiedIncluded() ?: return
                val qualifier = qualified.qualifier()
                if (qualifier == "classpath" || qualifier == "url") return
                val target = qualified.target() ?: return
                val targetText = target.stringValue() ?: return
                if (HoconIncludeResolver.resolveTargetFile(element) == null) {
                    val severity =
                        if (included.isRequired()) {
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        } else {
                            ProblemHighlightType.WEAK_WARNING
                        }
                    holder.registerProblem(target, "Cannot resolve included file '$targetText'", severity)
                }
            }
        }
}
