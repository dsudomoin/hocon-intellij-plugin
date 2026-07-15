package io.github.dsudomoin.hocon.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil

/** Elements that may appear as a field value or an array element. */
internal fun isHoconValue(e: PsiElement): Boolean =
    e is HObject || e is HArray || e is HNumber || e is HConcatenation ||
            e is HSubstitution || e is HStringValue || e is HUnquotedString ||
            e is HBoolean || e is HNull

private fun keyPartsText(owner: PsiElement): String =
    buildString {
        for (part in PsiTreeUtil.findChildrenOfType(owner, HKeyPart::class.java)) {
            val t = part.text
            when {
                t.length >= 6 && t.startsWith("\"\"\"") && t.endsWith("\"\"\"") ->
                    append(t.substring(3, t.length - 3))

                t.length >= 2 && t.first() == '"' && t.last() == '"' ->
                    append(unescapeHoconString(t))

                else -> append(t)
            }
        }
    }

/** Logical key text: concatenation of key parts, quoted parts unwrapped/unescaped. */
fun HFieldKey.keyText(): String = keyPartsText(this)

fun HSubstitutionKey.keyText(): String = keyPartsText(this)

/** Full dotted path of this field key from the config root (`a.b.c` or nested objects both → [a,b,c]). */
fun HFieldKey.fullPath(): List<String> {
    val keys = ArrayList<String>()
    var kf: HKeyedField? = PsiTreeUtil.getParentOfType(this, HKeyedField::class.java, true)
    while (kf != null) {
        kf.key()?.let { keys.add(it.keyText()) }
        kf = PsiTreeUtil.getParentOfType(kf, HKeyedField::class.java, true)
    }
    return keys.asReversed()
}

/** Presentable name of an object entry (first key of its keyed field) for structure view / breadcrumbs. */
fun HObjectField.presentableKey(): String = keyedField()?.key()?.keyText() ?: "?"

/** Leading `#`/`//` comment lines directly above this field (a blank line ends the doc block); null if none. */
fun HObjectField.docComment(): String? {
    val start = keyedField()?.key() ?: return null
    val lines = ArrayList<String>()
    var leaf: PsiElement? = PsiTreeUtil.prevLeaf(start)
    while (leaf != null) {
        when {
            leaf is PsiWhiteSpace -> if (leaf.text.count { it == '\n' } >= 2) break
            leaf is PsiComment -> lines.add(leaf.text.trimStart('#', '/').trim())
            else -> break
        }
        leaf = PsiTreeUtil.prevLeaf(leaf)
    }
    return if (lines.isEmpty()) null else lines.asReversed().joinToString("\n")
}

/** Renders a logical key name as valid HOCON key source: raw when safe, quoted+escaped otherwise. */
fun renderHoconKey(name: String): String =
    if (name.isNotEmpty() && name.all { it == '_' || it == '-' || it.isLetterOrDigit() }) {
        name
    } else {
        "\"" + name.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }

/** All [HObject] parts contributed by a value: a bare object, or the object parts of a concatenation. */
fun objectsOf(value: PsiElement?): List<HObject> =
    when (value) {
        is HObject -> listOf(value)
        is HConcatenation -> PsiTreeUtil.getChildrenOfTypeAsList(value, HObject::class.java)
        else -> emptyList()
    }

/** Logical string content of a value: unquoted verbatim, quoted unwrapped/unescaped, triple-quoted stripped. */
fun HStringValue.stringContent(): String {
    val t = text
    val q = '"'
    val triple = t.length >= 6 && t[0] == q && t[1] == q && t[2] == q &&
            t[t.length - 1] == q && t[t.length - 2] == q && t[t.length - 3] == q
    return when {
        triple -> t.substring(3, t.length - 3)
        t.length >= 2 && t.first() == q && t.last() == q -> unescapeHoconString(t)
        else -> t
    }
}

/**
 * Unescapes a HOCON quoted string. Accepts the raw token text (with or without surrounding quotes).
 * Handles the standard JSON escapes plus `\uXXXX`.
 */
fun unescapeHoconString(raw: String): String {
    var s = raw
    if (s.length >= 2 && s.first() == '"' && s.last() == '"') s = s.substring(1, s.length - 1)
    if (s.indexOf('\\') < 0) return s
    val sb = StringBuilder(s.length)
    var i = 0
    while (i < s.length) {
        val c = s[i]
        if (c.code == 92 && i + 1 < s.length) {
            when (val n = s[i + 1]) {
                '"' -> sb.append('"')
                '\\' -> sb.append('\\')
                '/' -> sb.append('/')
                'n' -> sb.append('\n')
                't' -> sb.append('\t')
                'r' -> sb.append('\r')
                'b' -> sb.append(8.toChar())
                'f' -> sb.append(12.toChar())
                'u' -> {
                    val hex = if (i + 6 <= s.length) s.substring(i + 2, i + 6) else ""
                    val code = if (hex.length == 4) hex.toIntOrNull(16) else null
                    if (code != null) {
                        sb.append(code.toChar())
                        i += 6
                        continue
                    }
                    sb.append(n)
                }

                else -> sb.append(n)
            }
            i += 2
        } else {
            sb.append(c)
            i++
        }
    }
    return sb.toString()
}
