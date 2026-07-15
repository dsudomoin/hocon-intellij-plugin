package io.github.dsudomoin.hocon.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import io.github.dsudomoin.hocon.psi.HObjectField
import io.github.dsudomoin.hocon.psi.fullPath
import io.github.dsudomoin.hocon.schema.HoconSchemaService

/**
 * Flags a key not present in the schema for its object level. Only fires when a [schema provider][HoconSchemaService]
 * describes that level, so the base plugin (which ships no providers) never reports anything.
 */
class HoconUnknownKeyInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is HObjectField) return
                val key = element.keyedField()?.key() ?: return
                val path = key.fullPath()
                if (path.isEmpty()) return
                val known = HoconSchemaService.keysAt(element.containingFile, path.dropLast(1))
                if (known.isEmpty()) return
                if (known.none { it.name == path.last() }) {
                    holder.registerProblem(key, "Unknown key '${path.last()}'", ProblemHighlightType.WEAK_WARNING)
                }
            }
        }
}
