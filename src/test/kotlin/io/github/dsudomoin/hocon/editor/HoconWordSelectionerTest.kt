package io.github.dsudomoin.hocon.editor

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.psi.HKeyPart

class HoconWordSelectionerTest : BasePlatformTestCase() {
    fun testCanSelectKeyPart() {
        myFixture.configureByText("a.conf", "abc = 1")
        val keyPart = PsiTreeUtil.findChildOfType(myFixture.file, HKeyPart::class.java)!!
        assertTrue(HoconWordSelectioner().canSelect(keyPart))
    }
}
