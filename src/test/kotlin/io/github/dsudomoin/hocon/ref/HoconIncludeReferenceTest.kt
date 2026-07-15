package io.github.dsudomoin.hocon.ref

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconIncludeReferenceTest : BasePlatformTestCase() {
    fun testIncludeResolvesToFile() {
        myFixture.addFileToProject("base.conf", "x = 1")
        val f = myFixture.configureByText("app.conf", "include \"ba<caret>se.conf\"")
        val ref = f.findReferenceAt(myFixture.caretOffset)!!
        assertEquals("base.conf", (ref.resolve() as PsiFile).name)
    }
}
