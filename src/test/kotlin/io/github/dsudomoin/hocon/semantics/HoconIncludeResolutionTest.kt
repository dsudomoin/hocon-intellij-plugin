package io.github.dsudomoin.hocon.semantics

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.lang.HoconFile
import io.github.dsudomoin.hocon.psi.HSubstitutionKey
import io.github.dsudomoin.hocon.psi.keyText

class HoconIncludeResolutionTest : BasePlatformTestCase() {
    private fun subKey(f: HoconFile): HSubstitutionKey =
        PsiTreeUtil.findChildOfType(f, HSubstitutionKey::class.java)!!

    fun testSubstitutionResolvesToField() {
        val f = myFixture.configureByText("t.conf", "host = localhost\nurl = " + "$" + "{host}") as HoconFile
        assertEquals(listOf("host"), HoconResolution.resolveSubstitutionKey(subKey(f)).map { it.keyText() })
    }

    fun testForwardReferenceResolves() {
        val f = myFixture.configureByText("t.conf", "url = " + "$" + "{host}\nhost = localhost") as HoconFile
        assertEquals(listOf("host"), HoconResolution.resolveSubstitutionKey(subKey(f)).map { it.keyText() })
    }

    fun testIncludeBringsKeysIntoScope() {
        myFixture.addFileToProject("base.conf", "shared = 1")
        val f = myFixture.configureByText(
            "app.conf",
            "include \"base.conf\"\nx = " + "$" + "{shared}",
        ) as HoconFile
        assertEquals(listOf("shared"), HoconResolution.resolveSubstitutionKey(subKey(f)).map { it.keyText() })
    }
}
