package io.github.dsudomoin.hocon.semantics

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import io.github.dsudomoin.hocon.psi.HInclude
import io.github.dsudomoin.hocon.psi.HKeyedField

/** Resolves `include` targets to [PsiFile]s and exposes their fields as an additional resolution scope. */
object HoconIncludeResolver {
    /**
     * Resolves the target file of an include: a bare or `file(...)` target relative to the including file's
     * directory, or a `classpath(...)` target against the module's classpath roots. Returns null for `url(...)`
     * or when the file is not found.
     */
    fun resolveTargetFile(include: HInclude): PsiFile? {
        val qualifier = include.included()?.qualifiedIncluded()?.qualifier()
        if (qualifier == "url") return null
        val path = include.targetText()?.takeIf { it.isNotEmpty() } ?: return null
        val hasExtension = path.substringAfterLast('/').contains('.')
        val roots =
            if (qualifier == "classpath") {
                HoconClasspathConfig.classpathRoots(include)
            } else {
                listOfNotNull(include.containingFile?.originalFile?.virtualFile?.parent)
            }
        for (root in roots) {
            val target =
                root.findFileByRelativePath(path)
                    ?: (if (hasExtension) null else root.findFileByRelativePath("$path.conf"))
            if (target != null && target.isValid && !target.isDirectory) {
                return PsiManager.getInstance(include.project).findFile(target)
            }
        }
        return null
    }

    fun includeScope(include: HInclude, guard: MutableSet<PsiFile>): Sequence<HKeyedField> {
        val file = resolveTargetFile(include) ?: return emptySequence()
        if (!guard.add(file)) return emptySequence()
        val entries = HoconResolution.rootEntries(file) ?: return emptySequence()
        return HoconPathResolver.topScope(entries, guard)
    }
}
