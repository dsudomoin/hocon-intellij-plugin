package io.github.dsudomoin.hocon.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.highlight.HoconColors
import io.github.dsudomoin.hocon.highlight.HoconValueHighlighter
import io.github.dsudomoin.hocon.lexer.HoconTokens
import io.github.dsudomoin.hocon.psi.HBoolean
import io.github.dsudomoin.hocon.psi.HInclude
import io.github.dsudomoin.hocon.psi.HKeyPart
import io.github.dsudomoin.hocon.psi.HNull
import io.github.dsudomoin.hocon.psi.HNumber
import io.github.dsudomoin.hocon.psi.HStringValue
import io.github.dsudomoin.hocon.psi.HSubstitutionKey
import io.github.dsudomoin.hocon.psi.HValuedField
import io.github.dsudomoin.hocon.psi.fullPath
import io.github.dsudomoin.hocon.psi.stringContent

/**
 * PSI-based highlighting for what the lexer cannot color: `UNQUOTED_CHARS` is the same token for keys,
 * numbers, booleans, null, plain string values and the `include` keyword, so these are colored by PSI.
 */
class HoconHighlightingAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is HKeyPart -> {
                val inSubstitution = PsiTreeUtil.getParentOfType(element, HSubstitutionKey::class.java) != null
                highlight(holder, element.textRange, if (inSubstitution) HoconColors.SUBSTITUTION else HoconColors.KEY)
            }

            is HNumber -> highlight(holder, element.textRange, HoconColors.NUMBER)
            is HBoolean, is HNull -> highlight(holder, element.textRange, HoconColors.KEYWORD)

            is HInclude -> {
                val keyword = element.node.findChildByType(HoconTokens.UNQUOTED_CHARS) ?: return
                highlight(holder, keyword.textRange, HoconColors.KEYWORD)
            }

            is HValuedField -> annotateValue(element, holder)
        }
    }

    /** Paints a string value when a [HoconValueHighlighter] assigns it a color (e.g. a schema enum constant). */
    private fun annotateValue(field: HValuedField, holder: AnnotationHolder) {
        val value = field.value() as? HStringValue ?: return
        val keyPath = field.key()?.fullPath() ?: return
        if (keyPath.isEmpty()) return
        val attributes = HoconValueHighlighter.resolve(field.containingFile, keyPath, value.stringContent()) ?: return
        highlight(holder, value.textRange, attributes)
    }

    private fun highlight(holder: AnnotationHolder, range: TextRange, key: TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(key)
            .create()
    }
}
