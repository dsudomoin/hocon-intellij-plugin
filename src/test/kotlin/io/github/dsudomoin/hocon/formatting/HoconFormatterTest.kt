package io.github.dsudomoin.hocon.formatting

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconFormatterTest : BasePlatformTestCase() {
    private fun doFormat(before: String, after: String) {
        myFixture.configureByText("a.conf", before)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        myFixture.checkResult(after)
    }

    fun testSeparatorSpacingNormalized() = doFormat("a  =  b", "a = b")

    fun testColonHugsKey() = doFormat("a  :  1", "a: 1")

    fun testColonNoSpaceBeforeGetsSpaceAfter() = doFormat("a:1", "a: 1")

    fun testEqualsKeepsSpacesAround() = doFormat("a=1", "a = 1")

    fun testPlusEqualsKeepsSpacesAround() = doFormat("a+=1", "a += 1")

    fun testSingleLineObjectStaysInline() = doFormat("p = {x: 10, y: 10}", "p = {x: 10, y: 10}")

    fun testMultilineObjectStaysMultiline() = doFormat("p {\n  x: 10\n  y: 10\n}", "p {\n  x: 10\n  y: 10\n}")

    fun testMultilineObjectGetsExpandedAndIndented() =
        doFormat("nested{a = 1\nb = 2}", "nested {\n  a = 1\n  b = 2\n}")

    fun testKeyGetsSpaceBeforeInlineObject() = doFormat("a{b = 1}", "a {b = 1}")

    fun testTrailingCommentStaysOnFieldLine() =
        doFormat(
            "app {\n  name = \"x\" # c\n  version = 1\n}",
            "app {\n  name = \"x\" # c\n  version = 1\n}",
        )

    fun testCommaSpacingNormalized() = doFormat("a = [1,2 ,3]", "a = [1, 2, 3]")

    fun testConcatenationPreserved() {
        val text = "a = foo " + "$" + "{b} bar"
        doFormat(text, text)
    }
}
