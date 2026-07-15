package io.github.dsudomoin.hocon.lang

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HoconFileTypeTest : BasePlatformTestCase() {
    fun testConfAndHoconExtensionsMapToHocon() {
        val ftm = FileTypeManager.getInstance()
        assertEquals(HoconFileType, ftm.getFileTypeByExtension("conf"))
        assertEquals(HoconFileType, ftm.getFileTypeByExtension("hocon"))
    }
}
