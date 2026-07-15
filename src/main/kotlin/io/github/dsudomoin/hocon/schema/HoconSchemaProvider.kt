package io.github.dsudomoin.hocon.schema

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile

/**
 * Extension point through which external plugins (e.g. a future Kora plugin) contribute a key schema for
 * HOCON files, without baking any framework knowledge into the base plugin. The base plugin ships no
 * providers; consumers (completion, documentation, the unknown-key inspection) degrade to no-ops.
 */
interface HoconSchemaProvider {
    /** The schema applicable to [file], or null if this provider does not handle it. */
    fun getSchema(file: PsiFile): HoconSchema?

    companion object {
        val EP_NAME: ExtensionPointName<HoconSchemaProvider> =
            ExtensionPointName.create("io.github.dsudomoin.hocon.schemaProvider")
    }
}
