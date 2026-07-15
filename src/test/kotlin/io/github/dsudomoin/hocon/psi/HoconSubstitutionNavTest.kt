package io.github.dsudomoin.hocon.psi

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.lang.HoconFile

class HoconSubstitutionNavTest : BasePlatformTestCase() {
    private fun file(text: String): HoconFile = myFixture.configureByText("t.conf", text) as HoconFile

    fun testSubstitutionPathKeys() {
        val f = file("a = " + "$" + "{b.c.d}")
        val sub = PsiTreeUtil.findChildOfType(f, HSubstitution::class.java)!!
        assertFalse(sub.isOptional())
        assertEquals(listOf("b", "c", "d"), sub.path()!!.allKeys().map { it.keyText() })
    }

    fun testOptionalSubstitution() {
        val f = file("a = " + "$" + "{?b}")
        val sub = PsiTreeUtil.findChildOfType(f, HSubstitution::class.java)!!
        assertTrue(sub.isOptional())
        assertEquals(listOf("b"), sub.path()!!.allKeys().map { it.keyText() })
    }

    fun testRequiredQualifiedIncludeTarget() {
        val f = file("include required(file(\"app.conf\"))")
        val inc = PsiTreeUtil.findChildOfType(f, HInclude::class.java)!!
        assertTrue(inc.included()!!.isRequired())
        assertEquals("file", inc.included()!!.qualifiedIncluded()!!.qualifier())
        assertEquals("app.conf", inc.targetText())
    }

    fun testBareIncludeTarget() {
        val f = file("include \"app.conf\"")
        val inc = PsiTreeUtil.findChildOfType(f, HInclude::class.java)!!
        assertFalse(inc.included()!!.isRequired())
        assertNull(inc.included()!!.qualifiedIncluded()!!.qualifier())
        assertEquals("app.conf", inc.targetText())
    }
}
