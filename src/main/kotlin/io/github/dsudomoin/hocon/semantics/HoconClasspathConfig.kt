package io.github.dsudomoin.hocon.semantics

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import io.github.dsudomoin.hocon.psi.HObjectEntries

/**
 * The classpath config files HOCON is merged with at runtime by `ConfigFactory.load`: every `reference.conf`
 * (including those inside library JARs) and every `application.conf` visible to the file's module. The search
 * is scoped to the module plus its dependencies and libraries, so configs from unrelated sibling modules do
 * not leak in. The file list is cached per module (or project); invalidated on root or PSI changes.
 */
object HoconClasspathConfig {
    private val NAMES = listOf("reference.conf", "application.conf")

    /** Root entries of every classpath config file in scope for [file], excluding [file] itself. */
    fun scope(file: PsiFile): List<HObjectEntries> {
        val self = file.originalFile.virtualFile
        val psiManager = PsiManager.getInstance(file.project)
        return files(file)
            .asSequence()
            .filter { it != self }
            .mapNotNull { psiManager.findFile(it) }
            .mapNotNull { HoconResolution.rootEntries(it) }
            .toList()
    }

    /**
     * Root entries of every `reference.conf` / `application.conf` on the classpath of [context]'s module,
     * for resolving a HOCON path from a non-HOCON context (e.g. a string literal in another language).
     */
    fun configScope(context: PsiElement): List<HObjectEntries> {
        val psiManager = PsiManager.getInstance(context.project)
        return files(context)
            .asSequence()
            .mapNotNull { psiManager.findFile(it) }
            .mapNotNull { HoconResolution.rootEntries(it) }
            .toList()
    }

    private fun files(context: PsiElement): List<VirtualFile> {
        val project = context.project
        val module = ModuleUtilCore.findModuleForPsiElement(context)
        val scope =
            module?.let { GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(it, true) }
                ?: GlobalSearchScope.projectScope(project)
        val holder: UserDataHolder = module ?: project
        return CachedValuesManager.getManager(project).getCachedValue(holder) {
            val vfs =
                NAMES
                    .flatMap { FilenameIndex.getVirtualFilesByName(it, scope) }
                    .filter { it.isValid && !it.isDirectory }
                    .distinct()
            CachedValueProvider.Result.create(
                vfs,
                ProjectRootModificationTracker.getInstance(project),
                PsiModificationTracker.getInstance(project),
            )
        }
    }

    /** Classpath roots of [context]'s module: source/resource roots plus dependency and library class roots. */
    fun classpathRoots(context: PsiElement): List<VirtualFile> {
        val module = ModuleUtilCore.findModuleForPsiElement(context) ?: return emptyList()
        return CachedValuesManager.getManager(module.project).getCachedValue(module) {
            val roots = LinkedHashSet<VirtualFile>()
            roots += ModuleRootManager.getInstance(module).getSourceRoots(true)
            OrderEnumerator.orderEntries(module).recursively().classes().roots.forEach { roots.add(it) }
            CachedValueProvider.Result.create(
                roots.toList(),
                ProjectRootModificationTracker.getInstance(module.project),
            )
        }
    }
}
