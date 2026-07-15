package io.github.dsudomoin.hocon.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import io.github.dsudomoin.hocon.psi.HSubstitution
import io.github.dsudomoin.hocon.psi.HoconElementFactory
import io.github.dsudomoin.hocon.semantics.HoconResolution

/**
 * Flags a non-optional `${path}` only when its root key exists in the config but the full path does not
 * (a likely typo, e.g. `${app.xuy}` where `app` exists but `xuy` does not). Substitutions rooted at an
 * unknown key (e.g. `${ENV}`) are left alone, since they usually resolve from system properties/environment.
 */
class HoconUnresolvedSubstitutionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is HSubstitution || element.isOptional()) return
                val keys = element.path()?.allKeys() ?: return
                if (keys.size < 2) return
                if (HoconResolution.resolveSubstitutionKey(keys.last()).isNotEmpty()) return
                val rootExists = HoconResolution.resolveSubstitutionKey(keys.first()).isNotEmpty()
                if (rootExists) {
                    holder.registerProblem(
                        element,
                        "Cannot resolve substitution '${element.path()?.text}'",
                        ProblemHighlightType.WEAK_WARNING,
                        MakeOptionalFix(),
                    )
                }
            }
        }

    private class MakeOptionalFix : LocalQuickFix {
        override fun getFamilyName(): String = "Make substitution optional"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val substitution = descriptor.psiElement as? HSubstitution ?: return
            if (substitution.isOptional()) return
            val pathText = substitution.path()?.text ?: return
            val replacement = HoconElementFactory.createSubstitution(project, pathText, optional = true) ?: return
            substitution.replace(replacement)
        }
    }
}
