package io.github.dsudomoin.hocon.folding

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconFoldingTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/testData"

    fun testObjectFolding() {
        myFixture.testFolding(getTestDataPath() + "/folding/Object.conf")
    }
}
