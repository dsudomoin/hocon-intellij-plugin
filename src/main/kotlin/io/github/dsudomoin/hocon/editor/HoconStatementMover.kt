package io.github.dsudomoin.hocon.editor

import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.lang.HoconFile
import io.github.dsudomoin.hocon.psi.HInclude
import io.github.dsudomoin.hocon.psi.HObjectField

/** Перемещает запись объекта (поле или include) целиком через соседнюю запись того же уровня. */
class HoconStatementMover : StatementUpDownMover() {
    override fun checkAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean {
        if (file !is HoconFile) return false
        val entry = entryAtCaret(editor, file) ?: return false
        val sibling = siblingEntry(entry, down)
        if (sibling == null) {
            info.prohibitMove()
            return true
        }
        info.toMove = lineRangeOf(editor, entry)
        info.toMove2 = lineRangeOf(editor, sibling)
        return true
    }

    private fun entryAtCaret(editor: Editor, file: PsiFile): PsiElement? {
        val offset = editor.caretModel.offset
        val el = file.findElementAt(offset) ?: file.findElementAt(offset - 1) ?: return null
        return PsiTreeUtil.getParentOfType(el, HObjectField::class.java, HInclude::class.java)
    }

    private fun siblingEntry(entry: PsiElement, down: Boolean): PsiElement? {
        var s: PsiElement? = if (down) entry.nextSibling else entry.prevSibling
        while (s != null) {
            if (s is HObjectField || s is HInclude) return s
            s = if (down) s.nextSibling else s.prevSibling
        }
        return null
    }

    private fun lineRangeOf(editor: Editor, element: PsiElement): LineRange {
        val doc = editor.document
        val start = doc.getLineNumber(element.textRange.startOffset)
        val end = doc.getLineNumber(element.textRange.endOffset)
        return LineRange(start, end + 1)
    }
}
