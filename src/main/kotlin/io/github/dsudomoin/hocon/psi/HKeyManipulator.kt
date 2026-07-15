package io.github.dsudomoin.hocon.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

/** Renames a field key by rebuilding it from the new logical name (quoting when required). */
class HKeyManipulator : AbstractElementManipulator<HFieldKey>() {
    override fun handleContentChange(element: HFieldKey, range: TextRange, newContent: String): HFieldKey {
        val newName = element.text.replaceRange(range.startOffset, range.endOffset, newContent)
        val newKey = HoconElementFactory.createFieldKey(element.project, renderHoconKey(unquoteKeySource(newName)))
            ?: return element
        return element.replace(newKey) as HFieldKey
    }
}

/** Substitution keys are always unquoted-or-quoted single segments; rebuild the same way. */
class HSubstitutionKeyManipulator : AbstractElementManipulator<HSubstitutionKey>() {
    override fun handleContentChange(
        element: HSubstitutionKey,
        range: TextRange,
        newContent: String,
    ): HSubstitutionKey {
        val newName = element.text.replaceRange(range.startOffset, range.endOffset, newContent)
        val newKey =
            HoconElementFactory.createSubstitutionKey(element.project, renderHoconKey(unquoteKeySource(newName)))
                ?: return element
        return element.replace(newKey) as HSubstitutionKey
    }
}

/** If [source] is a quoted key, decode it to its logical name; otherwise return as-is. */
private fun unquoteKeySource(source: String): String =
    if (source.length >= 2 && source.first() == '"' && source.last() == '"') unescapeHoconString(source) else source

/** Exposes the injectable inner range (inside the quotes) of a string value for language injection. */
class HStringValueManipulator : AbstractElementManipulator<HStringValue>() {
    override fun getRangeInElement(element: HStringValue): TextRange {
        val text = element.text
        return when {
            text.length >= 6 && text.startsWith("\"\"\"") && text.endsWith("\"\"\"") -> TextRange(3, text.length - 3)
            text.length >= 2 && text.first() == '"' && text.last() == '"' -> TextRange(1, text.length - 1)
            else -> TextRange(0, text.length)
        }
    }

    override fun handleContentChange(element: HStringValue, range: TextRange, newContent: String): HStringValue {
        val oldText = element.text
        val newText = oldText.substring(0, range.startOffset) + newContent + oldText.substring(range.endOffset)
        val newValue = HoconElementFactory.createStringValue(element.project, newText) ?: return element
        return element.replace(newValue) as HStringValue
    }
}
