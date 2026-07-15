package io.github.dsudomoin.hocon.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import io.github.dsudomoin.hocon.psi.*

/**
 * Flags a key declaration that is overridden by a later declaration of the same key at the same object level.
 * Object-valued declarations that merge with each other are NOT flagged (that is normal HOCON), only cases
 * where a later scalar/array/object declaration fully replaces an earlier one.
 */
class HoconDuplicateKeyInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is HObjectEntries) return
                val groups = LinkedHashMap<String, MutableList<HObjectField>>()
                for (field in element.objectFields()) {
                    val key = field.keyedField()?.key()?.keyText() ?: continue
                    groups.getOrPut(key) { ArrayList() }.add(field)
                }
                for ((key, group) in groups) {
                    if (group.size < 2) continue
                    for (i in group.indices) {
                        val declaration = group[i]
                        val declarationIsObject = isObjectValued(declaration)
                        val overridden =
                            (i + 1 until group.size).any { j ->
                                val later = group[j]
                                // '+=' appends rather than replaces, so it never overrides an earlier value.
                                !isAppend(later) && !(declarationIsObject && isObjectValued(later))
                            }
                        if (overridden) {
                            val keyElement = declaration.keyedField()?.key() ?: continue
                            holder.registerProblem(
                                keyElement,
                                "Key '$key' is overridden by a later definition",
                                ProblemHighlightType.WEAK_WARNING,
                            )
                        }
                    }
                }
            }
        }

    private fun isObjectValued(field: HObjectField): Boolean =
        when (val keyed = field.keyedField()) {
            is HPrefixedField -> true
            is HValuedField -> objectsOf(keyed.value()).isNotEmpty()
            else -> false
        }

    private fun isAppend(field: HObjectField): Boolean = (field.keyedField() as? HValuedField)?.isAppend() == true
}
