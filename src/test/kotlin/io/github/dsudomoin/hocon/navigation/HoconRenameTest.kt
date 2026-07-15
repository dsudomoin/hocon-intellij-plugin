package io.github.dsudomoin.hocon.navigation

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconRenameTest : BasePlatformTestCase() {
    fun testRenameAllDeclarationsAndUsages() {
        myFixture.configureByText("t.conf", "<caret>a = 1\na = 2\nb = " + "$" + "{a}")
        myFixture.renameElementAtCaret("x")
        myFixture.checkResult("x = 1\nx = 2\nb = " + "$" + "{x}")
    }

    fun testRenameNestedKeyUpdatesSubstitution() {
        myFixture.configureByText("t.conf", "s { <caret>port = 1 }\nx = " + "$" + "{s.port}")
        myFixture.renameElementAtCaret("p")
        myFixture.checkResult("s { p = 1 }\nx = " + "$" + "{s.p}")
    }
}
