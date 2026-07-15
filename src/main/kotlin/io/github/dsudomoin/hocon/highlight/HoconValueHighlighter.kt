package io.github.dsudomoin.hocon.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile

/**
 * Extension point through which external plugins paint HOCON values the base plugin cannot reason about
 * (e.g. framework enum constants), without baking any framework knowledge into the language plugin. It is
 * consulted by the highlighting annotator (for values already written in the file) and by value completion
 * (for the items it offers). The base plugin ships no implementations, so highlighting degrades to defaults.
 */
interface HoconValueHighlighter {
    /**
     * The text-attributes key to paint the value [valueText] of the key at [keyPath] with, or `null` to
     * leave default highlighting untouched. [keyPath] is the full dotted path of the key from the config
     * root; [valueText] is the value's logical string content (quotes unwrapped) — a candidate value when
     * called from completion.
     */
    fun valueHighlight(file: PsiFile, keyPath: List<String>, valueText: String): TextAttributesKey?

    companion object {
        val EP_NAME: ExtensionPointName<HoconValueHighlighter> =
            ExtensionPointName.create("io.github.dsudomoin.hocon.valueHighlighter")

        /** The first non-null attributes key any registered provider assigns to this value, or null. */
        fun resolve(file: PsiFile, keyPath: List<String>, valueText: String): TextAttributesKey? =
            EP_NAME.extensionList.firstNotNullOfOrNull { it.valueHighlight(file, keyPath, valueText) }
    }
}
