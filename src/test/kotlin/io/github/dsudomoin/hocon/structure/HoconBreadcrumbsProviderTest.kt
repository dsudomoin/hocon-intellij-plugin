package io.github.dsudomoin.hocon.structure

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.psi.HObjectField

class HoconBreadcrumbsProviderTest : BasePlatformTestCase() {
    fun testAcceptsFieldAndShowsKey() {
        myFixture.configureByText("a.conf", "a = 1")
        val field = PsiTreeUtil.findChildOfType(myFixture.file, HObjectField::class.java)!!
        val provider = HoconBreadcrumbsProvider()
        assertTrue(provider.acceptElement(field))
        assertEquals("a", provider.getElementInfo(field))
    }
}
