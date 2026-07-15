package io.github.dsudomoin.hocon.documentation

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.HSubstitution
import io.github.dsudomoin.hocon.psi.HSubstitutionKey
import io.github.dsudomoin.hocon.psi.keyText

class HoconDocumentationTest : BasePlatformTestCase() {
    fun testDocForKeyIncludesPathValueAndComment() {
        val f = myFixture.configureByText("t.conf", "# The database host\nhost = localhost")
        val key = PsiTreeUtil.findChildOfType(f, HFieldKey::class.java)!!
        val doc = HoconDocumentationProvider().generateDoc(key, null)!!
        assertTrue(doc.contains("host"))
        assertTrue(doc.contains("localhost"))
        assertTrue(doc.contains("The database host"))
    }

    fun testDocForSubstitutionResolvesTarget() {
        val f = myFixture.configureByText("t.conf", "host = localhost\nurl = " + "$" + "{host}")
        val subKey = PsiTreeUtil.findChildOfType(f, HSubstitutionKey::class.java)!!
        val doc = HoconDocumentationProvider().generateDoc(subKey, null)!!
        assertTrue(doc.contains("localhost"))
    }

    fun testQuickNavigateOverSubstitutionShowsResolvedValue() {
        val f = myFixture.configureByText(
            "t.conf",
            "app { name = \"hocon-demo\" }\ntitle = " + "$" + "{app.name}",
        )
        val provider = HoconDocumentationProvider()
        val subKey = PsiTreeUtil.collectElementsOfType(f, HSubstitutionKey::class.java).first { it.keyText() == "name" }
        val substitution = PsiTreeUtil.findChildOfType(f, HSubstitution::class.java)!!
        assertTrue(provider.getQuickNavigateInfo(subKey, subKey)!!.contains("hocon-demo"))
        assertTrue(provider.getQuickNavigateInfo(substitution, substitution)!!.contains("hocon-demo"))
    }

    fun testKeyWithSubstitutionValueShowsResolvedValue() {
        val f = myFixture.configureByText(
            "t.conf",
            "app { name = \"hocon-demo\" }\ntitle = " + "$" + "{app.name}",
        )
        val titleKey = PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == "title" }
        val info = HoconDocumentationProvider().getQuickNavigateInfo(titleKey, titleKey)!!
        assertTrue(info.contains("hocon-demo"))
        assertFalse(info.contains("app.name"))
    }

    fun testSelfReferentialValueDoesNotLoop() {
        val f = myFixture.configureByText("t.conf", "a = " + "$" + "{a}")
        val aKey = PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == "a" }
        assertNotNull(HoconDocumentationProvider().getQuickNavigateInfo(aKey, aKey))
    }

    fun testQuickNavigateInfoShowsPathAndValue() {
        val f = myFixture.configureByText("t.conf", "a { b = 42 }")
        val key = PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == "b" }
        assertEquals("a.b = 42", HoconDocumentationProvider().getQuickNavigateInfo(key, null))
    }
}
