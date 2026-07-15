package io.github.dsudomoin.hocon.semantics

import com.intellij.psi.PsiFile
import io.github.dsudomoin.hocon.psi.*

/**
 * Resolves a dotted key [path] within a set of [HObjectEntries] roots to the terminal [HFieldKey] declarations,
 * honoring HOCON merge semantics: a path may be declared many times (objects merge, values override), so ALL
 * matching declarations are collected. Object concatenation and `include`d files are expanded transitively.
 *
 * Known limitations (by design for phase 3): does not descend a path through a substitution-valued field, and
 * does not resolve `classpath(...)` / `url(...)` includes or reverse-includes.
 */
object HoconPathResolver {
    fun resolve(roots: List<HObjectEntries>, path: List<String>): List<HFieldKey> {
        if (path.isEmpty()) return emptyList()
        val out = ArrayList<HFieldKey>()
        val guard = HashSet<PsiFile>()
        val scope = roots.asSequence().flatMap { topScope(it, guard) }
        resolveInto(scope, path, 0, guard, out)
        return out
    }

    private fun resolveInto(
        scope: Sequence<HKeyedField>,
        path: List<String>,
        idx: Int,
        guard: MutableSet<PsiFile>,
        out: MutableList<HFieldKey>,
    ) {
        val seg = path[idx]
        for (field in scope) {
            val key = field.key() ?: continue
            if (key.keyText() != seg) continue
            if (idx == path.lastIndex) {
                out += key
            } else {
                resolveInto(subScope(field, guard), path, idx + 1, guard, out)
            }
        }
    }

    /**
     * Fields available as children of [path] (the completion scope). Empty path → the top scope.
     * Used to suggest key names inside `${a.b.<caret>}`.
     */
    fun childScope(roots: List<HObjectEntries>, path: List<String>): List<HKeyedField> {
        val guard = HashSet<PsiFile>()
        var scope: Sequence<HKeyedField> = roots.asSequence().flatMap { topScope(it, guard) }
        for (seg in path) {
            val matched = scope.filter { it.key()?.keyText() == seg }.toList()
            scope = matched.asSequence().flatMap { subScope(it, guard) }
        }
        return scope.toList()
    }

    private fun subScope(field: HKeyedField, guard: MutableSet<PsiFile>): Sequence<HKeyedField> =
        when (field) {
            is HPrefixedField -> field.subField()?.let { listOf(it).asSequence() } ?: emptySequence()
            is HValuedField ->
                objectsOf(field.value()).asSequence()
                    .flatMap { obj -> obj.entries()?.let { topScope(it, guard) } ?: emptySequence() }

            else -> emptySequence()
        }

    /** Candidate fields visible at this level: direct object fields plus include-expanded fields. */
    internal fun topScope(entries: HObjectEntries, guard: MutableSet<PsiFile>): Sequence<HKeyedField> {
        val direct = entries.objectFields().asSequence().mapNotNull { it.keyedField() }
        val included = entries.includes().asSequence().flatMap { HoconIncludeResolver.includeScope(it, guard) }
        return direct + included
    }
}
