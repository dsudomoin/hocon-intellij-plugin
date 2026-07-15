package io.github.dsudomoin.hocon.psi

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.lang.HoconFile

class HoconPsiNavTest : BasePlatformTestCase() {
    private fun file(text: String): HoconFile = myFixture.configureByText("test.conf", text) as HoconFile

    fun testValuedFieldExposesKeyAndValue() {
        val f = file("a = 1")
        val keyed = PsiTreeUtil.findChildOfType(f, HObjectField::class.java)!!.keyedField()!!
        assertTrue(keyed is HValuedField)
        assertEquals("a", keyed.key()!!.keyText())
        assertTrue((keyed as HValuedField).value() is HNumber)
    }

    fun testPrefixedFieldChain() {
        val f = file("a.b.c = 1")
        val a = PsiTreeUtil.findChildOfType(f, HObjectField::class.java)!!.keyedField() as HPrefixedField
        assertEquals("a", a.key()!!.keyText())
        val b = a.subField() as HPrefixedField
        assertEquals("b", b.key()!!.keyText())
        val c = b.subField() as HValuedField
        assertEquals("c", c.key()!!.keyText())
    }

    fun testObjectEntriesExposeFields() {
        val f = file("s {\n  host = x\n  port = 1\n}")
        val obj = PsiTreeUtil.findChildOfType(f, HObject::class.java)!!
        val names = obj.entries()!!.objectFields().map { it.keyedField()!!.key()!!.keyText() }
        assertEquals(listOf("host", "port"), names)
    }

    fun testObjectValueEntries() {
        val f = file("s { host = x }")
        val s = PsiTreeUtil.findChildOfType(f, HObjectField::class.java)!!.keyedField() as HValuedField
        val obj = s.value() as HObject
        assertEquals(listOf("host"), obj.entries()!!.objectFields().map { it.keyedField()!!.key()!!.keyText() })
    }

    fun testQuotedKeyUnwrapped() {
        val f = file("\"a b\" = 1")
        val key = PsiTreeUtil.findChildOfType(f, HFieldKey::class.java)!!
        assertEquals("a b", key.keyText())
    }

    fun testFullPathNestedObjects() {
        val f = file("a {\n  b {\n    c = 1\n  }\n}")
        val cKey = PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == "c" }
        assertEquals(listOf("a", "b", "c"), cKey.fullPath())
    }

    fun testFullPathPrefixed() {
        val f = file("a.b.c = 1")
        val cKey = PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == "c" }
        assertEquals(listOf("a", "b", "c"), cKey.fullPath())
    }

    fun testFieldKeyPathText() {
        val f = file("a { b { c = 1 } }")
        val cKey = PsiTreeUtil.collectElementsOfType(f, HFieldKey::class.java).first { it.keyText() == "c" }
        assertEquals("a.b.c", cKey.pathText())
    }
}
