package io.github.dsudomoin.hocon.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object HoconFileType : LanguageFileType(HoconLanguage) {
    override fun getName(): String = "HOCON"

    override fun getDescription(): String = "HOCON configuration"

    override fun getDefaultExtension(): String = "conf"

    override fun getIcon(): Icon = HoconIcons.FILE
}
