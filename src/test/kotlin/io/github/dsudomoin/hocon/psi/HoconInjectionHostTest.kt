package io.github.dsudomoin.hocon.psi

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconInjectionHostTest : BasePlatformTestCase() {
    private fun stringValue(text: String): HStringValue {
        val f = myFixture.configureByText("t.conf", text)
        return PsiTreeUtil.findChildOfType(f, HStringValue::class.java)!!
    }

    fun testQuotedStringIsValidHost() {
        assertTrue((stringValue("sql = \"select 1\"") as PsiLanguageInjectionHost).isValidHost)
    }

    fun testUnquotedStringIsNotValidHost() {
        assertFalse((stringValue("name = plainvalue") as PsiLanguageInjectionHost).isValidHost)
    }

    fun testInjectionValueRangeIsInsideQuotes() {
        val sv = stringValue("sql = \"select 1\"")
        val range = ElementManipulators.getManipulator(sv).getRangeInElement(sv)
        assertEquals("select 1", range.substring(sv.text))
    }

    fun testUpdateTextReplacesContent() {
        val sv = stringValue("sql = \"a\"")
        WriteCommandAction.runWriteCommandAction(project) {
            (sv as PsiLanguageInjectionHost).updateText("\"select 1\"")
        }
        assertEquals("sql = \"select 1\"", myFixture.file.text)
    }
}
