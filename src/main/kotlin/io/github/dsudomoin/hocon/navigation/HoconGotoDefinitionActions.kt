package io.github.dsudomoin.hocon.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.lang.HoconLanguage
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.fullPath

/**
 * Navigates between successive definitions of the same HOCON path within a file (cycling at the ends).
 * Bound to the Super Method / Implementation shortcuts, which are meaningless in HOCON files.
 */
abstract class HoconGotoDefinitionActionBase(private val forward: Boolean) : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = occurrences(e)?.let { it.size >= 2 } == true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val occurrences = occurrences(e) ?: return
        if (occurrences.size < 2) return
        val caret = editor.caretModel.offset
        val target =
            if (forward) {
                occurrences.firstOrNull { it.textOffset > caret } ?: occurrences.first()
            } else {
                occurrences.lastOrNull { it.textOffset < caret } ?: occurrences.last()
            }
        editor.caretModel.moveToOffset(target.textOffset)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
    }

    /** All field keys in the file sharing the path of the key under the caret, ordered by offset; null if none. */
    private fun occurrences(e: AnActionEvent): List<HFieldKey>? {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return null
        val path = pathAtCaret(editor, file) ?: return null
        return PsiTreeUtil.collectElementsOfType(file, HFieldKey::class.java)
            .filter { it.fullPath() == path }
            .sortedBy { it.textOffset }
    }

    private fun pathAtCaret(editor: Editor, file: PsiFile): List<String>? {
        if (file.language != HoconLanguage) return null
        val element = file.findElementAt(editor.caretModel.offset) ?: return null
        return PsiTreeUtil.getParentOfType(element, HFieldKey::class.java)?.fullPath()?.takeIf { it.isNotEmpty() }
    }
}

class HoconGotoNextDefinitionAction : HoconGotoDefinitionActionBase(forward = true)

class HoconGotoPrevDefinitionAction : HoconGotoDefinitionActionBase(forward = false)
