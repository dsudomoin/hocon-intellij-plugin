package io.github.dsudomoin.hocon.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import io.github.dsudomoin.hocon.index.HoconKeyIndex
import io.github.dsudomoin.hocon.psi.HFieldKey
import io.github.dsudomoin.hocon.psi.keyText

/** "Go to Symbol" over HOCON keys, backed by [HoconKeyIndex]. */
class HoconGotoSymbolContributor : ChooseByNameContributorEx {
    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        FileBasedIndex.getInstance().processAllKeys(HoconKeyIndex.NAME, processor, scope, filter)
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters,
    ) {
        val scope = parameters.searchScope
        val project = scope.project ?: return
        val psiManager = PsiManager.getInstance(project)
        FileBasedIndex.getInstance().processFilesContainingAllKeys(
            HoconKeyIndex.NAME,
            setOf(name),
            scope,
            null,
        ) processFile@{ virtualFile ->
            val psi = psiManager.findFile(virtualFile) ?: return@processFile true
            for (key in PsiTreeUtil.collectElementsOfType(psi, HFieldKey::class.java)) {
                if (key.keyText() == name && !processor.process(key)) return@processFile false
            }
            true
        }
    }
}
