package io.github.dsudomoin.hocon.editor

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconCommenterTest : BasePlatformTestCase() {
    fun testLineCommentAddsHash() {
        myFixture.configureByText("a.conf", "<caret>a = b")
        myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE)
        assertTrue(myFixture.editor.document.text.startsWith("#"))
    }
}
