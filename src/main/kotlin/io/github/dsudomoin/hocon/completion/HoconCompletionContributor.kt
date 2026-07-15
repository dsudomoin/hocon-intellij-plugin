package io.github.dsudomoin.hocon.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import io.github.dsudomoin.hocon.highlight.HoconValueHighlighter
import io.github.dsudomoin.hocon.lang.HoconLanguage
import io.github.dsudomoin.hocon.psi.*
import io.github.dsudomoin.hocon.schema.HoconKeySpec
import io.github.dsudomoin.hocon.schema.HoconSchemaService
import io.github.dsudomoin.hocon.semantics.HoconPathResolver
import io.github.dsudomoin.hocon.semantics.HoconResolution

/**
 * Completes substitution paths (`${a.b.<caret>}` → child keys of `a.b`) and the `true`/`false`/`null`
 * value keywords. Include-target paths are completed by the file references contributed in phase 3.
 */
class HoconCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(HoconLanguage),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    val position = parameters.position
                    val substitution = PsiTreeUtil.getParentOfType(position, HSubstitution::class.java)
                    if (substitution != null) {
                        addSubstitutionKeys(position, substitution, result)
                    } else if (PsiTreeUtil.getParentOfType(position, HFieldKey::class.java) != null) {
                        addSchemaKeys(position, result)
                    } else if (isInUnquotedValue(position)) {
                        addValueCompletions(position, result)
                    }
                }
            },
        )
    }

    private fun addSubstitutionKeys(position: PsiElement, substitution: HSubstitution, result: CompletionResultSet) {
        val keys = substitution.path()?.allKeys().orEmpty()
        val currentKey = keys.lastOrNull { PsiTreeUtil.isAncestor(it, position, false) }
        val prefixCount = if (currentKey != null) keys.indexOf(currentKey) else keys.size
        val prefix = keys.subList(0, prefixCount.coerceAtLeast(0)).map { it.keyText() }
        val roots = HoconResolution.toplevelScope(position.containingFile)
        HoconPathResolver.childScope(roots, prefix)
            .mapNotNull { it.key()?.keyText() }
            .toSortedSet()
            .forEach { result.addElement(LookupElementBuilder.create(it)) }
    }

    private fun addSchemaKeys(position: PsiElement, result: CompletionResultSet) {
        val objectPath = enclosingObjectPath(position)
        // Offer full nested paths relative to the current object (`bar.baz.someBazString`), not just direct
        // children — valid HOCON path expressions, and shows the whole path in the popup.
        for (spec in HoconSchemaService.keysAt(position.containingFile, objectPath)) {
            addSpecPaths(spec, "", result)
        }
    }

    private fun addSpecPaths(spec: HoconKeySpec, prefix: String, result: CompletionResultSet) {
        val path = if (prefix.isEmpty()) spec.name else "$prefix.${spec.name}"
        val tail =
            buildString {
                spec.default?.let { append(" = ").append(it) }
                spec.description?.let { append("  (").append(it).append(')') }
            }
        var element = LookupElementBuilder.create(path).withIcon(AllIcons.Nodes.Property)
        spec.type?.let { element = element.withTypeText(it) }
        if (tail.isNotEmpty()) element = element.withTailText(tail, true)
        result.addElement(element)
        for (child in spec.children) addSpecPaths(child, path, result)
    }

    /** In value position: offer the key's enum values (from the schema), otherwise the boolean/null keywords. */
    private fun addValueCompletions(position: PsiElement, result: CompletionResultSet) {
        val fieldPath = PsiTreeUtil.getParentOfType(position, HValuedField::class.java)?.key()?.fullPath()
        val spec = fieldPath?.let { HoconSchemaService.keySpec(position.containingFile, it) }
        if (fieldPath != null && spec != null && spec.enumValues.isNotEmpty()) {
            val scheme = EditorColorsManager.getInstance().globalScheme
            for (value in spec.enumValues) {
                var element = LookupElementBuilder.create(value).withIcon(AllIcons.Nodes.Enum)
                val color = HoconValueHighlighter.resolve(position.containingFile, fieldPath, value)
                    ?.let { scheme.getAttributes(it)?.foregroundColor }
                if (color != null) element = element.withItemTextForeground(color)
                result.addElement(element)
            }
        } else {
            VALUE_KEYWORDS.forEach { result.addElement(LookupElementBuilder.create(it)) }
        }
    }

    private fun enclosingObjectPath(position: PsiElement): List<String> {
        val obj = PsiTreeUtil.getParentOfType(position, HObject::class.java) ?: return emptyList()
        val ownerField = PsiTreeUtil.getParentOfType(obj, HValuedField::class.java) ?: return emptyList()
        return ownerField.key()?.fullPath() ?: emptyList()
    }

    private fun isInUnquotedValue(position: PsiElement): Boolean =
        PsiTreeUtil.getParentOfType(position, HStringValue::class.java) != null &&
                PsiTreeUtil.getParentOfType(position, HFieldKey::class.java) == null

    private companion object {
        val VALUE_KEYWORDS = listOf("true", "false", "null")
    }
}
