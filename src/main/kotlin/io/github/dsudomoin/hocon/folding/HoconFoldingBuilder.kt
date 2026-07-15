package io.github.dsudomoin.hocon.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.lexer.HoconTokens
import io.github.dsudomoin.hocon.parser.HoconElements
import io.github.dsudomoin.hocon.psi.HArray
import io.github.dsudomoin.hocon.psi.HObject

class HoconFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val out = ArrayList<FoldingDescriptor>()
        for (o in PsiTreeUtil.findChildrenOfType(root, HObject::class.java)) addIfMultiline(o, document, out)
        for (a in PsiTreeUtil.findChildrenOfType(root, HArray::class.java)) addIfMultiline(a, document, out)
        for (leaf in PsiTreeUtil.findChildrenOfType(root, PsiElement::class.java)) {
            if (leaf.node?.elementType == HoconTokens.MULTILINE_STRING) addIfMultiline(leaf, document, out)
        }
        return out.toTypedArray()
    }

    private fun addIfMultiline(element: PsiElement, document: Document, out: MutableList<FoldingDescriptor>) {
        val node = element.node ?: return
        val range = element.textRange
        if (range.length > 2 &&
            document.getLineNumber(range.startOffset) != document.getLineNumber(range.endOffset - 1)
        ) {
            out.add(FoldingDescriptor(node, range))
        }
    }

    override fun getPlaceholderText(node: ASTNode): String =
        when (node.elementType) {
            HoconElements.OBJECT -> "{...}"
            HoconElements.ARRAY -> "[...]"
            else -> "..."
        }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
