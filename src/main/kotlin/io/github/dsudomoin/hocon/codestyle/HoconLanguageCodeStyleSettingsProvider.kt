package io.github.dsudomoin.hocon.codestyle

import com.intellij.application.options.IndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import io.github.dsudomoin.hocon.lang.HoconLanguage

class HoconLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage(): Language = HoconLanguage

    override fun getIndentOptionsEditor(): IndentOptionsEditor = IndentOptionsEditor()

    override fun getCodeSample(settingsType: SettingsType): String =
        "server {\n  host = localhost\n  port = 8080\n  tags = [a, b, c]\n}\n"

    override fun customizeDefaults(
        commonSettings: CommonCodeStyleSettings,
        indentOptions: CommonCodeStyleSettings.IndentOptions,
    ) {
        indentOptions.INDENT_SIZE = 2
        indentOptions.CONTINUATION_INDENT_SIZE = 2
        indentOptions.TAB_SIZE = 2
    }
}
