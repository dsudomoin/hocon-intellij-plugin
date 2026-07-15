package io.github.dsudomoin.hocon.navigation

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.fullPath

class HoconGotoDefinitionActionsTest : BasePlatformTestCase() {
    fun testGotoNextCyclesForward() {
        // Offsets: first x = 0, second x = 6, third x = 12.
        myFixture.configureByText("a.conf", "<caret>x = 1\nx = 2\nx = 3")
        myFixture.performEditorAction("Hocon.GotoNextDefinition")
        assertEquals(6, myFixture.caretOffset)
        myFixture.performEditorAction("Hocon.GotoNextDefinition")
        assertEquals(12, myFixture.caretOffset)
        myFixture.performEditorAction("Hocon.GotoNextDefinition")
        assertEquals(0, myFixture.caretOffset) // wraps to first
    }

    fun testGotoPrevCyclesBackward() {
        myFixture.configureByText("a.conf", "x = 1\nx = 2\n<caret>x = 3")
        myFixture.performEditorAction("Hocon.GotoPrevDefinition")
        assertEquals(6, myFixture.caretOffset)
        myFixture.performEditorAction("Hocon.GotoPrevDefinition")
        assertEquals(0, myFixture.caretOffset)
        myFixture.performEditorAction("Hocon.GotoPrevDefinition")
        assertEquals(12, myFixture.caretOffset) // wraps to last
    }

    fun testMatchesByPathAcrossFlatAndNested() {
        myFixture.configureByText("a.conf", "a.<caret>b = 1\na { b = 2 }")
        myFixture.performEditorAction("Hocon.GotoNextDefinition")
        val key =
            PsiTreeUtil.getParentOfType(myFixture.file.findElementAt(myFixture.caretOffset), HFieldKey::class.java)
        assertNotNull("caret should land on a field key", key)
        assertEquals(listOf("a", "b"), key!!.fullPath())
        assertTrue("caret moved off the flat 'b'", myFixture.caretOffset != 2)
    }

    fun testNoOpForSingleOccurrence() {
        myFixture.configureByText("a.conf", "<caret>x = 1\ny = 2")
        myFixture.performEditorAction("Hocon.GotoNextDefinition")
        assertEquals(0, myFixture.caretOffset) // only one 'x' → no movement
    }
}
