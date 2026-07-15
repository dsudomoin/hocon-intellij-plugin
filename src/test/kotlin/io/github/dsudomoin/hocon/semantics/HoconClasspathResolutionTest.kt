package io.github.dsudomoin.hocon.semantics

import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.dsudomoin.hocon.psi.HInclude
import io.github.dsudomoin.hocon.psi.HSubstitutionKey
import io.github.dsudomoin.hocon.psi.keyText

class HoconClasspathResolutionTest : BasePlatformTestCase() {
    private val sub = "$" + "{akka.timeout}"
    private val subPrefix = "$" + "{akka."

    fun testCompletionOffersReferenceConfKeys() {
        myFixture.addFileToProject("reference.conf", "akka.timeout = 5s")
        myFixture.configureByText("application.conf", "x = " + subPrefix + "<caret>}")
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, "timeout")
    }

    fun testSubstitutionResolvesToReferenceConf() {
        myFixture.addFileToProject("reference.conf", "akka.timeout = 5s")
        val f = myFixture.configureByText("application.conf", "x = " + sub)
        val timeoutKey =
            PsiTreeUtil.collectElementsOfType(f, HSubstitutionKey::class.java).first { it.keyText() == "timeout" }
        val results = (timeoutKey.reference as PsiPolyVariantReference).multiResolve(false)
        assertTrue("should resolve", results.isNotEmpty())
        assertTrue(
            "should resolve into reference.conf",
            results.any { it.element?.containingFile?.name == "reference.conf" },
        )
    }

    fun testEditedFileNotDuplicatedAsClasspathConfig() {
        myFixture.addFileToProject("reference.conf", "akka.timeout = 5s")
        val f = myFixture.configureByText("application.conf", "akka.timeout = 1s\nx = " + sub)
        val timeoutKey =
            PsiTreeUtil.collectElementsOfType(f, HSubstitutionKey::class.java).first { it.keyText() == "timeout" }
        val results = (timeoutKey.reference as PsiPolyVariantReference).multiResolve(false)
        val localCount = results.count { it.element?.containingFile?.name == "application.conf" }
        assertEquals("local definition must appear exactly once", 1, localCount)
    }

    fun testClasspathIncludeResolves() {
        myFixture.addFileToProject("common.conf", "a = 1")
        val f = myFixture.configureByText("application.conf", "include classpath(\"common.conf\")")
        val include = PsiTreeUtil.findChildOfType(f, HInclude::class.java)!!
        assertEquals("common.conf", HoconIncludeResolver.resolveTargetFile(include)?.name)
    }
}
