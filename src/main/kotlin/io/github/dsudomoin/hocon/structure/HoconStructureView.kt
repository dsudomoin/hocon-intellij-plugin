package io.github.dsudomoin.hocon.structure

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.lang.HoconFile
import io.github.dsudomoin.hocon.psi.HObject
import io.github.dsudomoin.hocon.psi.HObjectEntries
import io.github.dsudomoin.hocon.psi.HObjectField
import io.github.dsudomoin.hocon.psi.presentableKey

class HoconStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder =
        object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel =
                HoconStructureViewModel(psiFile)
        }
}

class HoconStructureViewModel(psiFile: PsiFile) :
    StructureViewModelBase(psiFile, HoconStructureViewElement(psiFile)),
    StructureViewModel.ElementInfoProvider {
    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement?): Boolean = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement?): Boolean = false
}

class HoconStructureViewElement(private val element: PsiElement) : StructureViewTreeElement, SortableTreeElement {
    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        (element as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (element as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource(): Boolean = (element as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey(): String = presentableText()

    override fun getPresentation(): ItemPresentation =
        PresentationData(presentableText(), null, AllIcons.Nodes.Property, null)

    override fun getChildren(): Array<TreeElement> {
        val children: List<TreeElement> = childFields(element).map { HoconStructureViewElement(it) }
        return children.toTypedArray()
    }

    private fun presentableText(): String =
        when (element) {
            is HoconFile -> element.name
            is HObjectField -> element.presentableKey()
            else -> element.text
        }

    private fun childFields(element: PsiElement): List<HObjectField> {
        val entries =
            when (element) {
                is HoconFile -> PsiTreeUtil.getChildOfType(element, HObjectEntries::class.java)
                is HObjectField ->
                    PsiTreeUtil.findChildOfType(element, HObject::class.java)
                        ?.let { PsiTreeUtil.getChildOfType(it, HObjectEntries::class.java) }

                else -> null
            } ?: return emptyList()
        return PsiTreeUtil.getChildrenOfTypeAsList(entries, HObjectField::class.java)
    }
}
