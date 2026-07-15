package io.github.dsudomoin.hocon.index

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import io.github.dsudomoin.hocon.lang.HoconFile
import io.github.dsudomoin.hocon.lang.HoconFileType
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.keyText

/** Project-wide index of HOCON key simple names → used by goto symbol. */
class HoconKeyIndex : ScalarIndexExtension<String>() {
    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> =
        DataIndexer { inputData ->
            val psi = inputData.psiFile as? HoconFile ?: return@DataIndexer emptyMap()
            val result = HashMap<String, Void?>()
            for (key in PsiTreeUtil.collectElementsOfType(psi, HFieldKey::class.java)) {
                result[key.keyText()] = null
            }
            result
        }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = DefaultFileTypeSpecificInputFilter(HoconFileType)

    override fun dependsOnFileContent(): Boolean = true

    companion object {
        val NAME: ID<String, Void> = ID.create("io.github.dsudomoin.hocon.key.index")
    }
}
