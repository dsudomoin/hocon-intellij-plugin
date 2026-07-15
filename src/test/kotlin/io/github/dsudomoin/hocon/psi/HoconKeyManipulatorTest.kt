package io.github.dsudomoin.hocon.psi

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconKeyManipulatorTest : BasePlatformTestCase() {
    private fun key(text: String): HFieldKey {
        val f = myFixture.configureByText("t.conf", text)
        return PsiTreeUtil.findChildOfType(f, HFieldKey::class.java)!!
    }

    fun testRenameFieldKey() {
        val k = key("aaa = 1")
        WriteCommandAction.runWriteCommandAction(project) { k.setName("bbb") }
        assertEquals("bbb = 1", myFixture.file.text)
    }

    fun testRenameToSpecialCharQuotes() {
        val k = key("a = 1")
        WriteCommandAction.runWriteCommandAction(project) { k.setName("a b") }
        assertEquals("\"a b\" = 1", myFixture.file.text)
    }
}
