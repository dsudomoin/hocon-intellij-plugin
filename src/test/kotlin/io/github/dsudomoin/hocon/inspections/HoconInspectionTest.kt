package io.github.dsudomoin.hocon.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconInspectionTest : BasePlatformTestCase() {
    private fun hasProblem(fragment: String): Boolean =
        myFixture.doHighlighting().any { it.description?.contains(fragment) == true }

    fun testTypoUnderKnownRootHighlighted() {
        myFixture.enableInspections(HoconUnresolvedSubstitutionInspection::class.java)
        myFixture.configureByText("t.conf", "app { name = x }\nurl = " + "$" + "{app.missing}")
        assertTrue(hasProblem("Cannot resolve substitution"))
    }

    fun testUnknownRootNotHighlighted() {
        myFixture.enableInspections(HoconUnresolvedSubstitutionInspection::class.java)
        myFixture.configureByText("t.conf", "url = " + "$" + "{ENV}")
        assertFalse(hasProblem("Cannot resolve substitution"))
    }

    fun testUnknownRootedPathNotHighlighted() {
        myFixture.enableInspections(HoconUnresolvedSubstitutionInspection::class.java)
        myFixture.configureByText("t.conf", "url = " + "$" + "{external.thing}")
        assertFalse(hasProblem("Cannot resolve substitution"))
    }

    fun testResolvedSubstitutionNotHighlighted() {
        myFixture.enableInspections(HoconUnresolvedSubstitutionInspection::class.java)
        myFixture.configureByText("t.conf", "app { name = x }\nurl = " + "$" + "{app.name}")
        assertFalse(hasProblem("Cannot resolve substitution"))
    }

    fun testOptionalSubstitutionNotHighlighted() {
        myFixture.enableInspections(HoconUnresolvedSubstitutionInspection::class.java)
        myFixture.configureByText("t.conf", "app { name = x }\nurl = " + "$" + "{?app.missing}")
        assertFalse(hasProblem("Cannot resolve substitution"))
    }

    fun testMakeOptionalQuickFix() {
        myFixture.enableInspections(HoconUnresolvedSubstitutionInspection::class.java)
        myFixture.configureByText("t.conf", "app { name = x }\nurl = " + "$" + "{app.mis<caret>sing}")
        myFixture.launchAction(myFixture.findSingleIntention("Make substitution optional"))
        myFixture.checkResult("app { name = x }\nurl = " + "$" + "{?app.missing}")
    }

    fun testDuplicateScalarKeyHighlighted() {
        myFixture.enableInspections(HoconDuplicateKeyInspection::class.java)
        myFixture.configureByText("t.conf", "a = 1\na = 2")
        assertTrue(hasProblem("overridden"))
    }

    fun testMergedObjectsNotHighlighted() {
        myFixture.enableInspections(HoconDuplicateKeyInspection::class.java)
        myFixture.configureByText("t.conf", "a { x = 1 }\na { y = 2 }")
        assertFalse(hasProblem("overridden"))
    }

    fun testAppendIsNotOverride() {
        myFixture.enableInspections(HoconDuplicateKeyInspection::class.java)
        myFixture.configureByText("t.conf", "tags = [a]\ntags += b")
        assertFalse(hasProblem("overridden"))
    }

    fun testUnresolvedIncludeHighlighted() {
        myFixture.enableInspections(HoconUnresolvedIncludeInspection::class.java)
        myFixture.configureByText("app.conf", "include \"missing.conf\"")
        assertTrue(hasProblem("Cannot resolve included file"))
    }

    fun testResolvedIncludeNotHighlighted() {
        myFixture.enableInspections(HoconUnresolvedIncludeInspection::class.java)
        myFixture.addFileToProject("base.conf", "x = 1")
        myFixture.configureByText("app.conf", "include \"base.conf\"")
        assertFalse(hasProblem("Cannot resolve included file"))
    }
}
