package io.github.dsudomoin.hocon.navigation

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconFindUsagesTest : BasePlatformTestCase() {
    fun testFindUsagesFromFieldKey() {
        myFixture.configureByText(
            "t.conf",
            "<caret>host = x\nurl = " + "$" + "{host}\nurl2 = " + "$" + "{host}",
        )
        val usages = myFixture.findUsages(myFixture.elementAtCaret)
        assertEquals(2, usages.size)
    }
}
