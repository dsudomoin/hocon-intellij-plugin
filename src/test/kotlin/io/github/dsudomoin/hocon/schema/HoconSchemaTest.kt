package io.github.dsudomoin.hocon.schema

import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.documentation.HoconDocumentationProvider
import io.github.dsudomoin.hocon.highlight.HoconValueHighlighter
import io.github.dsudomoin.hocon.inspections.HoconUnknownKeyInspection
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.keyText

class HoconSchemaTest : BasePlatformTestCase() {
    private class FakeSchemaProvider : HoconSchemaProvider {
        override fun getSchema(file: PsiFile): HoconSchema =
            TreeHoconSchema(
                listOf(
                    HoconKeySpec(
                        "server",
                        "Object",
                        "The server config",
                        children = listOf(
                            HoconKeySpec("port", "Int", "The listening port", default = "8080"),
                            HoconKeySpec("host", "String", "The bind host"),
                        ),
                    ),
                    HoconKeySpec("logging", "Object", "Logging config"),
                    HoconKeySpec("mode", "Mode", enumValues = listOf("FAST", "SAFE")),
                ),
            )
    }

    override fun setUp() {
        super.setUp()
        HoconSchemaProvider.EP_NAME.point.registerExtension(FakeSchemaProvider(), testRootDisposable)
    }

    fun testSchemaKeyCompletionInsideObject() {
        myFixture.configureByText("t.conf", "server {\n  <caret>\n}")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "port", "host")
    }

    fun testRootSchemaKeyCompletion() {
        myFixture.configureByText("t.conf", "<caret>")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "server", "logging")
    }

    fun testDeepNestedPathsOffered() {
        myFixture.configureByText("t.conf", "<caret>")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "server.port", "server.host")
    }

    fun testSchemaDocumentation() {
        val f = myFixture.configureByText("t.conf", "server { port = 8080 }")
        val key = PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == "port" }
        val doc = HoconDocumentationProvider().generateDoc(key, null)!!
        assertTrue(doc.contains("Int"))
        assertTrue(doc.contains("The listening port"))
    }

    fun testSchemaDefaultShownInDoc() {
        val f = myFixture.configureByText("t.conf", "server { port = 1 }")
        val key = PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == "port" }
        val doc = HoconDocumentationProvider().generateDoc(key, null)!!
        assertTrue(doc.contains("8080"))
    }

    fun testDescriptionShownInParensInTail() {
        myFixture.configureByText("t.conf", "server {\n  <caret>\n}")
        myFixture.completeBasic()
        val port = myFixture.lookupElements!!.first { it.lookupString == "port" }
        val presentation = LookupElementPresentation()
        port.renderElement(presentation)
        assertTrue(presentation.tailText!!.contains("(The listening port)"))
    }

    fun testEnumValueCompletion() {
        myFixture.configureByText("t.conf", "mode = <caret>")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "FAST", "SAFE")
    }

    fun testEnumValueCompletionUsesHighlighterColor() {
        val key =
            TextAttributesKey.createTextAttributesKey("TEST_ENUM_LOOKUP", DefaultLanguageHighlighterColors.CONSTANT)
        HoconValueHighlighter.EP_NAME.point.registerExtension(
            object : HoconValueHighlighter {
                override fun valueHighlight(
                    file: PsiFile,
                    keyPath: List<String>,
                    valueText: String
                ): TextAttributesKey? =
                    if (valueText == "FAST" || valueText == "SAFE") key else null
            },
            testRootDisposable,
        )
        myFixture.configureByText("t.conf", "mode = <caret>")
        myFixture.completeBasic()
        val fast = myFixture.lookupElements!!.first { it.lookupString == "FAST" }
        val presentation = LookupElementPresentation()
        fast.renderElement(presentation)
        val expected = EditorColorsManager.getInstance().globalScheme.getAttributes(key)?.foregroundColor
        assertEquals(expected, presentation.itemTextForeground)
    }

    fun testUnknownKeyHighlighted() {
        myFixture.enableInspections(HoconUnknownKeyInspection::class.java)
        myFixture.configureByText("t.conf", "server { bogus = 1 }")
        assertTrue(myFixture.doHighlighting().any { it.description?.contains("Unknown key 'bogus'") == true })
    }

    fun testKnownKeyNotHighlighted() {
        myFixture.enableInspections(HoconUnknownKeyInspection::class.java)
        myFixture.configureByText("t.conf", "server { port = 1 }")
        assertFalse(myFixture.doHighlighting().any { it.description?.contains("Unknown key") == true })
    }
}
