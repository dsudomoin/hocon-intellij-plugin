package io.github.dsudomoin.hocon.navigation

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.keyText

class HoconQualifiedNameTest : BasePlatformTestCase() {
    private fun keyNamed(text: String, name: String): HFieldKey {
        val f = myFixture.configureByText("t.conf", text)
        return PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == name }
    }

    fun testQualifiedNameNestedObject() =
        assertEquals("a.b.c", HoconQualifiedNameProvider().getQualifiedName(keyNamed("a { b { c = 1 } }", "c")))

    fun testQualifiedNamePrefixed() =
        assertEquals("a.b.c", HoconQualifiedNameProvider().getQualifiedName(keyNamed("a.b.c = 1", "c")))
}
