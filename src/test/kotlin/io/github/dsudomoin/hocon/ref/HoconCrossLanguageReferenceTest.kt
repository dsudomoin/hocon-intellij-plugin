package io.github.dsudomoin.hocon.ref

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import io.github.dsudomoin.hocon.psi.HStringValue

class HoconCrossLanguageReferenceTest : LightJavaCodeInsightFixtureTestCase() {

    private inline fun <reified T : Any> firstReferenceOfType(file: PsiFile): T? {
        var result: T? = null
        file.accept(
            object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (result == null) {
                        result = element.references.filterIsInstance<T>().firstOrNull()
                    }
                    super.visitElement(element)
                }
            },
        )
        return result
    }

    fun testJavaStringLiteralResolvesToHoconKey() {
        myFixture.addFileToProject("application.conf", "app.name = demo")
        val f = myFixture.configureByText("A.java", "class A { String s = \"app.name\"; }")
        val ref = firstReferenceOfType<HoconStringLiteralPathReference>(f)
        assertNotNull("expected a HOCON path reference on the Java literal", ref)
        assertTrue(
            (ref as PsiPolyVariantReference).multiResolve(false).any {
                it.element?.containingFile?.name == "application.conf"
            },
        )
    }

    fun testKotlinStringLiteralResolvesToHoconKey() {
        myFixture.addFileToProject("application.conf", "app.name = demo")
        val f = myFixture.configureByText("A.kt", "val s = \"app.name\"")
        val ref = firstReferenceOfType<HoconStringLiteralPathReference>(f)
        assertNotNull("expected a HOCON path reference on the Kotlin literal", ref)
        assertTrue(
            (ref as PsiPolyVariantReference).multiResolve(false).any {
                it.element?.containingFile?.name == "application.conf"
            },
        )
    }

    fun testUnresolvablePathHasNoReference() {
        myFixture.addFileToProject("application.conf", "app.name = demo")
        val f = myFixture.configureByText("A.java", "class A { String s = \"no.such.key\"; }")
        assertNull(firstReferenceOfType<HoconStringLiteralPathReference>(f))
    }

    fun testHoconStringResolvesToClass() {
        myFixture.addClass("package com.example;\npublic class Foo {}")
        val f = myFixture.configureByText("a.conf", "impl = \"com.example.Foo\"")
        val value = PsiTreeUtil.collectElementsOfType(f, HStringValue::class.java).first { it.text.contains("Foo") }
        val ref = value.references.filterIsInstance<HoconClassReference>().firstOrNull()
        assertNotNull("expected a class reference in the HOCON string", ref)
        assertEquals("Foo", (ref!!.resolve() as? PsiClass)?.name)
    }

    fun testHoconStringNonClassHasNoReference() {
        val f = myFixture.configureByText("a.conf", "x = \"not.a.class.name\"")
        val value = PsiTreeUtil.collectElementsOfType(f, HStringValue::class.java).first()
        assertTrue(value.references.none { it is HoconClassReference })
    }

    fun testJavaLiteralCompletionOffersConfigKeys() {
        myFixture.addFileToProject("application.conf", "app.name = x\napp.version = y")
        myFixture.configureByText("A.java", "class A { String s = \"app.<caret>\"; }")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "name", "version")
    }

    fun testKotlinLiteralCompletionOffersConfigKeys() {
        myFixture.addFileToProject("application.conf", "app.name = x\napp.enabled = y")
        myFixture.configureByText("A.kt", "val s = \"app.<caret>\"")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "name", "enabled")
    }
}
