package io.github.dsudomoin.hocon.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import io.github.dsudomoin.hocon.parser.HoconElements

interface HoconPsiElement : PsiElement

abstract class HoconPsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), HoconPsiElement

/** A field that carries a key: either a path-prefix ([HPrefixedField]) or a terminal ([HValuedField]). */
interface HKeyedField : HoconPsiElement {
    fun key(): HFieldKey?
}

object HoconPsiFactory {
    fun create(node: ASTNode): PsiElement = when (node.elementType) {
        HoconElements.OBJECT -> HObject(node)
        HoconElements.OBJECT_ENTRIES -> HObjectEntries(node)
        HoconElements.INCLUDE -> HInclude(node)
        HoconElements.INCLUDED -> HIncluded(node)
        HoconElements.QUALIFIED_INCLUDED -> HQualifiedIncluded(node)
        HoconElements.OBJECT_FIELD -> HObjectField(node)
        HoconElements.PREFIXED_FIELD -> HPrefixedField(node)
        HoconElements.VALUED_FIELD -> HValuedField(node)
        HoconElements.PATH -> HPath(node)
        HoconElements.FIELD_KEY -> HFieldKey(node)
        HoconElements.SUBSTITUTION_KEY -> HSubstitutionKey(node)
        HoconElements.KEY_PART -> HKeyPart(node)
        HoconElements.ARRAY -> HArray(node)
        HoconElements.SUBSTITUTION -> HSubstitution(node)
        HoconElements.CONCATENATION -> HConcatenation(node)
        HoconElements.UNQUOTED_STRING -> HUnquotedString(node)
        HoconElements.STRING_VALUE -> HStringValue(node)
        HoconElements.INCLUDE_TARGET -> HIncludeTarget(node)
        HoconElements.NUMBER -> HNumber(node)
        HoconElements.NULL -> HNull(node)
        HoconElements.BOOLEAN -> HBoolean(node)
        else -> ASTWrapperPsiElement(node)
    }
}
