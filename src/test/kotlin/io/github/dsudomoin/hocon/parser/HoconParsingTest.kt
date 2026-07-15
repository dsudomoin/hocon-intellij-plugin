package io.github.dsudomoin.hocon.parser

import com.intellij.testFramework.ParsingTestCase

class HoconParsingTest : ParsingTestCase("parser", "conf", HoconParserDefinition()) {
    override fun getTestDataPath(): String = "src/test/testData"

    fun testSimpleField() = doTest(true, false)

    fun testObjectField() = doTest(true, false)

    fun testArrayValue() = doTest(true, false)

    fun testSubstitution() = doTest(true, false)

    fun testConcatenation() = doTest(true, false)

    fun testInclude() = doTest(true, false)

    fun testPrefixedPath() = doTest(true, false)

    fun testLiterals() = doTest(true, false)
}
