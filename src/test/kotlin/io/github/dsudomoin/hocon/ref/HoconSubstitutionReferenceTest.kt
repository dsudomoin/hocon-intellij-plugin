package io.github.dsudomoin.hocon.ref

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.HSubstitutionKey
import io.github.dsudomoin.hocon.psi.keyText

class HoconSubstitutionReferenceTest : BasePlatformTestCase() {
    fun testSubstitutionReferenceResolvesToDeclaration() {
        val f = myFixture.configureByText("t.conf", "host = localhost\nurl = " + "$" + "{host}")
        val subKey = PsiTreeUtil.findChildOfType(f, HSubstitutionKey::class.java)!!
        assertEquals("host", (subKey.reference!!.resolve() as HFieldKey).keyText())
    }

    fun testNestedSubstitutionReference() {
        val f = myFixture.configureByText("t.conf", "s { p = 1 }\nx = " + "$" + "{s.p}")
        val pKey = PsiTreeUtil.collectElementsOfType(f, HSubstitutionKey::class.java).first { it.keyText() == "p" }
        assertEquals("p", (pKey.reference!!.resolve() as HFieldKey).keyText())
    }

    fun testUnresolvedSubstitutionReference() {
        val f = myFixture.configureByText("t.conf", "url = " + "$" + "{missing}")
        val subKey = PsiTreeUtil.findChildOfType(f, HSubstitutionKey::class.java)!!
        assertNull(subKey.reference!!.resolve())
    }
}
