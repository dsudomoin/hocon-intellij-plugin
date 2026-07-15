package io.github.dsudomoin.hocon.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconCompletionTest : BasePlatformTestCase() {
    fun testSubstitutionChildKeyCompletion() {
        myFixture.configureByText(
            "t.conf",
            "database {\n  host = x\n  port = 1\n}\nurl = " + "$" + "{database.<caret>}",
        )
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "host", "port")
    }

    fun testTopLevelSubstitutionCompletion() {
        myFixture.configureByText("t.conf", "alpha = 1\nbeta = 2\nx = " + "$" + "{<caret>}")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "alpha", "beta")
    }

    fun testValueKeywordCompletion() {
        myFixture.configureByText("t.conf", "enabled = <caret>")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "true", "false", "null")
    }
}
