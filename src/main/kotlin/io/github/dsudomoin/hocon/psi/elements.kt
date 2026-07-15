package io.github.dsudomoin.hocon.psi

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import io.github.dsudomoin.hocon.lexer.HoconTokens
import io.github.dsudomoin.hocon.ref.HKeyReference
import io.github.dsudomoin.hocon.ref.HoconIncludeReferenceSet

class HObject(node: ASTNode) : HoconPsiElementImpl(node) {
    fun entries(): HObjectEntries? = PsiTreeUtil.getChildOfType(this, HObjectEntries::class.java)
}

class HObjectEntries(node: ASTNode) : HoconPsiElementImpl(node) {
    fun objectFields(): List<HObjectField> = PsiTreeUtil.getChildrenOfTypeAsList(this, HObjectField::class.java)

    fun includes(): List<HInclude> = PsiTreeUtil.getChildrenOfTypeAsList(this, HInclude::class.java)
}

class HInclude(node: ASTNode) : HoconPsiElementImpl(node) {
    fun included(): HIncluded? = PsiTreeUtil.getChildOfType(this, HIncluded::class.java)

    /** The include target string (unescaped), unwrapping `required(...)` and `file/url/classpath(...)`. */
    fun targetText(): String? = included()?.qualifiedIncluded()?.target()?.stringValue()
}

class HIncluded(node: ASTNode) : HoconPsiElementImpl(node) {
    /** `include required(...)` — the `required` keyword is a direct token child of this node. */
    fun isRequired(): Boolean = node.findChildByType(HoconTokens.UNQUOTED_CHARS)?.text == "required"

    fun qualifiedIncluded(): HQualifiedIncluded? =
        PsiTreeUtil.getChildOfType(this, HQualifiedIncluded::class.java)
}

class HQualifiedIncluded(node: ASTNode) : HoconPsiElementImpl(node) {
    /** `file` / `url` / `classpath`, or `null` for a bare quoted include. */
    fun qualifier(): String? = node.findChildByType(HoconTokens.UNQUOTED_CHARS)?.text

    fun target(): HIncludeTarget? = PsiTreeUtil.getChildOfType(this, HIncludeTarget::class.java)
}

class HObjectField(node: ASTNode) : HoconPsiElementImpl(node) {
    /** The single keyed field this entry wraps (prefixed or valued). */
    fun keyedField(): HKeyedField? = children.firstNotNullOfOrNull { it as? HKeyedField }
}

class HPrefixedField(node: ASTNode) : HoconPsiElementImpl(node), HKeyedField {
    override fun key(): HFieldKey? = PsiTreeUtil.getChildOfType(this, HFieldKey::class.java)

    /** The nested field for the next path segment (`a.b.c` → `a`'s subField is `b`). */
    fun subField(): HKeyedField? = children.firstNotNullOfOrNull { it as? HKeyedField }
}

class HValuedField(node: ASTNode) : HoconPsiElementImpl(node), HKeyedField {
    override fun key(): HFieldKey? = PsiTreeUtil.getChildOfType(this, HFieldKey::class.java)

    /** The value element (object / array / number / concatenation / substitution / string / boolean / null). */
    fun value(): PsiElement? = children.lastOrNull { isHoconValue(it) }

    /** True for `key += value`, which appends to (rather than replaces) the previous value. */
    fun isAppend(): Boolean = node.findChildByType(HoconTokens.PLUS_EQUALS) != null
}

class HPath(node: ASTNode) : HoconPsiElementImpl(node) {
    /** All substitution keys of this path in source order (`${a.b.c}` → [a, b, c]). */
    fun allKeys(): List<HSubstitutionKey> {
        val inner = PsiTreeUtil.getChildOfType(this, HPath::class.java)
        val own = children.firstNotNullOfOrNull { it as? HSubstitutionKey }
        return (inner?.allKeys() ?: emptyList()) + listOfNotNull(own)
    }
}

class HFieldKey(node: ASTNode) : HoconPsiElementImpl(node), PsiNameIdentifierOwner {
    /** Full dotted path of this key from the config root, e.g. `"a.b.c"` (convenience for external consumers). */
    fun pathText(): String = fullPath().joinToString(".")

    override fun getName(): String = keyText()

    override fun getNameIdentifier(): PsiElement = this

    override fun setName(name: String): PsiElement {
        val manipulator = ElementManipulators.getManipulator(this)
            ?: throw IncorrectOperationException("No manipulator registered for HOCON field key")
        return manipulator.handleContentChange(this, name) ?: this
    }

    override fun getPresentation(): ItemPresentation =
        object : ItemPresentation {
            override fun getPresentableText(): String = fullPath().joinToString(".")

            override fun getLocationString(): String? = containingFile?.name

            override fun getIcon(unused: Boolean) = null
        }
}

class HSubstitutionKey(node: ASTNode) : HoconPsiElementImpl(node) {
    override fun getReferences(): Array<PsiReference> = arrayOf(HKeyReference(this))

    override fun getReference(): PsiReference? = references.firstOrNull()
}

class HKeyPart(node: ASTNode) : HoconPsiElementImpl(node)

class HArray(node: ASTNode) : HoconPsiElementImpl(node)

class HSubstitution(node: ASTNode) : HoconPsiElementImpl(node) {
    fun path(): HPath? = PsiTreeUtil.getChildOfType(this, HPath::class.java)

    fun isOptional(): Boolean = node.findChildByType(HoconTokens.QMARK) != null
}

class HConcatenation(node: ASTNode) : HoconPsiElementImpl(node)

class HUnquotedString(node: ASTNode) : HoconPsiElementImpl(node)

class HStringValue(node: ASTNode) : HoconPsiElementImpl(node), PsiLanguageInjectionHost {
    override fun isValidHost(): Boolean {
        val type = node.firstChildNode?.elementType
        return type == HoconTokens.QUOTED_STRING || type == HoconTokens.MULTILINE_STRING
    }

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val replacement = HoconElementFactory.createStringValue(project, text) ?: return this
        return replace(replacement) as PsiLanguageInjectionHost
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<HStringValue> = HoconLiteralTextEscaper(this)

    /** Surfaces `psi.referenceContributor` references (e.g. the optional class reference in jvm-support.xml). */
    override fun getReferences(): Array<PsiReference> = ReferenceProvidersRegistry.getReferencesFromProviders(this)
}

class HIncludeTarget(node: ASTNode) : HoconPsiElementImpl(node) {
    /** Content of the quoted target string, unescaped, without surrounding quotes. */
    fun stringValue(): String? {
        val quoted = node.findChildByType(HoconTokens.QUOTED_STRING)?.text ?: return null
        return unescapeHoconString(quoted)
    }

    override fun getReferences(): Array<PsiReference> = HoconIncludeReferenceSet.referencesFor(this)

    override fun getReference(): PsiReference? = references.lastOrNull()
}

class HNumber(node: ASTNode) : HoconPsiElementImpl(node)

class HNull(node: ASTNode) : HoconPsiElementImpl(node)

class HBoolean(node: ASTNode) : HoconPsiElementImpl(node)
