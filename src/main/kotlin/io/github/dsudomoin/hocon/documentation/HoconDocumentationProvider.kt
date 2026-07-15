package io.github.dsudomoin.hocon.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.psi.*
import io.github.dsudomoin.hocon.schema.HoconSchemaService
import io.github.dsudomoin.hocon.semantics.HoconResolution

/** Hover / quick documentation for HOCON keys and substitutions: full path, value, and doc comment. */
class HoconDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val key = asFieldKey(element) ?: return null
        val path = key.fullPath().joinToString(".")
        val value = valueText(key)
        val doc = PsiTreeUtil.getParentOfType(key, HObjectField::class.java)?.docComment()
        return buildString {
            append(DocumentationMarkup.DEFINITION_START)
            append(StringUtil.escapeXmlEntities(path))
            append(DocumentationMarkup.DEFINITION_END)
            if (value != null) {
                append(DocumentationMarkup.CONTENT_START)
                append("= ").append(StringUtil.escapeXmlEntities(value))
                append(DocumentationMarkup.CONTENT_END)
            }
            if (doc != null) {
                append(DocumentationMarkup.CONTENT_START)
                append(StringUtil.escapeXmlEntities(doc).replace("\n", "<br/>"))
                append(DocumentationMarkup.CONTENT_END)
            }
            val spec = HoconSchemaService.keySpec(key.containingFile, key.fullPath())
            if (spec != null && (spec.type != null || spec.description != null || spec.default != null)) {
                append(DocumentationMarkup.CONTENT_START)
                spec.type?.let { append("Type: ").append(StringUtil.escapeXmlEntities(it)).append("<br/>") }
                spec.default?.let { append("Default: ").append(StringUtil.escapeXmlEntities(it)).append("<br/>") }
                spec.description?.let { append(StringUtil.escapeXmlEntities(it)) }
                append(DocumentationMarkup.CONTENT_END)
            }
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        val key = asFieldKey(element) ?: return null
        val path = key.fullPath().joinToString(".")
        val value = valueText(key)
        return if (value != null) "$path = $value" else path
    }

    // Resolves a documentable element to a key declaration: a field key directly, a substitution key, or any
    // element inside a substitution (so Ctrl-hover anywhere on a substitution shows the resolved value).
    private fun asFieldKey(element: PsiElement?): HFieldKey? {
        if (element == null) return null
        if (element is HFieldKey) return element
        val substitutionKey = element as? HSubstitutionKey
            ?: PsiTreeUtil.getParentOfType(element, HSubstitutionKey::class.java, false)
        if (substitutionKey != null) return HoconResolution.resolveSubstitutionKey(substitutionKey).firstOrNull()
        val substitution = element as? HSubstitution
            ?: PsiTreeUtil.getParentOfType(element, HSubstitution::class.java, false)
        val lastKey = substitution?.path()?.allKeys()?.lastOrNull() ?: return null
        return HoconResolution.resolveSubstitutionKey(lastKey).firstOrNull()
    }

    // When a value is itself a substitution (`title = ${'$'}{app.name}`), follow it to the resolved value so hover
    // shows `"hocon-demo"` rather than the raw `${'$'}{app.name}`. Guarded against reference cycles (`a = ${'$'}{a}`).
    private fun valueText(key: HFieldKey, visited: MutableSet<HFieldKey> = HashSet()): String? {
        if (!visited.add(key)) return null
        return when (val keyed = key.parent) {
            is HValuedField -> {
                val value = keyed.value() ?: return null
                if (value is HSubstitution) {
                    resolvedSubstitutionValue(value, visited) ?: StringUtil.first(value.text.trim(), 200, true)
                } else {
                    StringUtil.first(value.text.trim(), 200, true)
                }
            }

            is HPrefixedField -> "{ … }"
            else -> null
        }
    }

    private fun resolvedSubstitutionValue(substitution: HSubstitution, visited: MutableSet<HFieldKey>): String? {
        val lastKey = substitution.path()?.allKeys()?.lastOrNull() ?: return null
        val declaration = HoconResolution.resolveSubstitutionKey(lastKey).firstOrNull() ?: return null
        return valueText(declaration, visited)
    }
}
