package io.github.dsudomoin.hocon.navigation

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconGotoSymbolTest : BasePlatformTestCase() {
    fun testProcessNamesFindsKeys() {
        myFixture.configureByText("t.conf", "server {\n  port = 8080\n}")
        val names = ArrayList<String>()
        HoconGotoSymbolContributor().processNames({ names.add(it); true }, GlobalSearchScope.allScope(project), null)
        assertTrue("expected 'port' in $names", names.contains("port"))
        assertTrue("expected 'server' in $names", names.contains("server"))
    }
}
