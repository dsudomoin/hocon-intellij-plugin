package io.github.dsudomoin.hocon.schema

import com.intellij.psi.PsiFile

/** Aggregates all registered [HoconSchemaProvider]s for a file. */
object HoconSchemaService {
    private fun schemas(file: PsiFile): List<HoconSchema> =
        HoconSchemaProvider.EP_NAME.extensionList.mapNotNull { it.getSchema(file) }

    /** Known key specs directly under [path] (empty path = root), merged across all providers. */
    fun keysAt(file: PsiFile, path: List<String>): List<HoconKeySpec> =
        schemas(file).flatMap { it.keysAt(path) }

    /** The spec for the key at the full [path], or null if no schema describes it. */
    fun keySpec(file: PsiFile, path: List<String>): HoconKeySpec? {
        if (path.isEmpty()) return null
        return keysAt(file, path.dropLast(1)).firstOrNull { it.name == path.last() }
    }
}
