package io.github.dsudomoin.hocon.editor

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconStatementMoverTest : BasePlatformTestCase() {
    fun testMoveFieldDown() {
        myFixture.configureByText("a.conf", "<caret>a = 1\nb = 2\n")
        myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_DOWN_ACTION)
        myFixture.checkResult("b = 2\na = 1\n")
    }

    fun testMoveFieldUp() {
        myFixture.configureByText("a.conf", "a = 1\n<caret>b = 2\n")
        myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_UP_ACTION)
        myFixture.checkResult("b = 2\na = 1\n")
    }
}
