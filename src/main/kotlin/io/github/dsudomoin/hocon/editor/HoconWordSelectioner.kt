package io.github.dsudomoin.hocon.editor

import com.intellij.codeInsight.editorActions.wordSelection.AbstractWordSelectioner
import com.intellij.psi.PsiElement
import io.github.dsudomoin.hocon.psi.HKeyPart

class HoconWordSelectioner : AbstractWordSelectioner() {
    override fun canSelect(e: PsiElement): Boolean = e is HKeyPart
}
