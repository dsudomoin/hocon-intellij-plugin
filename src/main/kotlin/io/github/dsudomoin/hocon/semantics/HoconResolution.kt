package io.github.dsudomoin.hocon.semantics

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.psi.*

/** Entry points that tie the resolver to concrete files and substitution usages. */
object HoconResolution {
    /** The root object entries of a HOCON file (top-level `k = v` list, or the body of a top-level `{ }`). */
    fun rootEntries(file: PsiFile): HObjectEntries? =
        PsiTreeUtil.getChildOfType(file, HObjectEntries::class.java)
            ?: PsiTreeUtil.getChildOfType(file, HObject::class.java)?.entries()

    /**
     * The scope a substitution resolves against: the config root of the file plus every `reference.conf` /
     * `application.conf` on the file's classpath (mirroring `ConfigFactory.load` merge semantics). Includes
     * are expanded lazily by the resolver during descent.
     */
    fun toplevelScope(file: PsiFile): List<HObjectEntries> =
        listOfNotNull(rootEntries(file)) + HoconClasspathConfig.scope(file)

    /** Resolves a substitution key to the declarations of its path prefix (`${a.b.c}` on `b` → decls of `a.b`). */
    fun resolveSubstitutionKey(key: HSubstitutionKey): List<HFieldKey> {
        val path = PsiTreeUtil.getParentOfType(key, HSubstitution::class.java)?.path() ?: return emptyList()
        val keys = path.allKeys()
        val idx = keys.indexOf(key)
        if (idx < 0) return emptyList()
        val prefix = keys.subList(0, idx + 1).map { it.keyText() }
        return HoconPathResolver.resolve(toplevelScope(key.containingFile), prefix)
    }
}
