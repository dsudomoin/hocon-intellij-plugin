package io.github.dsudomoin.hocon.ref

import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.lexer.HoconTokens
import io.github.dsudomoin.hocon.psi.HIncludeTarget
import io.github.dsudomoin.hocon.psi.HQualifiedIncluded
import io.github.dsudomoin.hocon.semantics.HoconClasspathConfig

/**
 * File references for an `include "path"` / `include file("path")` target, resolved relative to the including
 * file's directory. `classpath(...)` / `url(...)` targets contribute no references (not supported in phase 3).
 */
class HoconIncludeReferenceSet(
    pathString: String,
    element: HIncludeTarget,
    startInElement: Int,
    private val classpath: Boolean,
) : FileReferenceSet(pathString, element, startInElement, null, true) {

    override fun computeDefaultContexts(): Collection<PsiFileSystemItem> {
        val psiManager = PsiManager.getInstance(element.project)
        if (classpath) {
            return HoconClasspathConfig.classpathRoots(element).mapNotNull { psiManager.findDirectory(it) }
        }
        val parent = element.containingFile?.originalFile?.virtualFile?.parent ?: return emptyList()
        return listOfNotNull(psiManager.findDirectory(parent))
    }

    companion object {
        fun referencesFor(target: HIncludeTarget): Array<PsiReference> {
            val qualifier = PsiTreeUtil.getParentOfType(target, HQualifiedIncluded::class.java)?.qualifier()
            if (qualifier == "url") return PsiReference.EMPTY_ARRAY
            val quoted = target.node.findChildByType(HoconTokens.QUOTED_STRING) ?: return PsiReference.EMPTY_ARRAY
            val quotedText = quoted.text
            if (quotedText.length < 2) return PsiReference.EMPTY_ARRAY
            val inner = quotedText.substring(1, quotedText.length - 1)
            val startInElement = (quoted.startOffset - target.node.startOffset) + 1
            val refs = HoconIncludeReferenceSet(inner, target, startInElement, qualifier == "classpath").allReferences
            return Array(refs.size) { refs[it] }
        }
    }
}
