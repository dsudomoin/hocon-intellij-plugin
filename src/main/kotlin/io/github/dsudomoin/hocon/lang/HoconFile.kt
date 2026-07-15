package io.github.dsudomoin.hocon.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class HoconFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, HoconLanguage) {
    override fun getFileType(): FileType = HoconFileType

    override fun toString(): String = "HOCON File"
}
