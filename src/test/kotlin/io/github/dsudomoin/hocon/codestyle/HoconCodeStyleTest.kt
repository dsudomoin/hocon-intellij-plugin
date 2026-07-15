package io.github.dsudomoin.hocon.codestyle

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconCodeStyleTest : BasePlatformTestCase() {
    fun testObjectIndentIsTwoSpaces() {
        myFixture.configureByText("a.conf", "a {\nb = 1\n}")
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        myFixture.checkResult("a {\n  b = 1\n}")
    }
}
