package io.github.dsudomoin.hocon.formatting

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import io.github.dsudomoin.hocon.lexer.HoconTokenSets
import io.github.dsudomoin.hocon.lexer.HoconTokens
import io.github.dsudomoin.hocon.parser.HoconElements

class HoconBlock(
    node: ASTNode,
    private val spacingBuilder: SpacingBuilder,
    private val indent: Indent,
) : AbstractBlock(node, null, null) {
    override fun getIndent(): Indent = indent

    override fun buildChildren(): List<Block> {
        val result = ArrayList<Block>()
        var child = node.firstChildNode
        while (child != null) {
            val type = child.elementType
            if (type != TokenType.WHITE_SPACE && !HoconTokenSets.WHITESPACE.contains(type) && child.textLength > 0) {
                result.add(HoconBlock(child, spacingBuilder, childIndent(type)))
            }
            child = child.treeNext
        }
        return result
    }

    private fun childIndent(childType: IElementType): Indent =
        when (node.elementType) {
            HoconElements.OBJECT ->
                if (childType == HoconTokens.LBRACE || childType == HoconTokens.RBRACE) {
                    Indent.getNoneIndent()
                } else {
                    Indent.getNormalIndent()
                }

            HoconElements.ARRAY ->
                if (childType == HoconTokens.LBRACKET || childType == HoconTokens.RBRACKET) {
                    Indent.getNoneIndent()
                } else {
                    Indent.getNormalIndent()
                }

            else -> Indent.getNoneIndent()
        }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        val type1 = (child1 as? HoconBlock)?.node?.elementType
        val type2 = (child2 as? HoconBlock)?.node?.elementType

        // A key directly followed by its object value gets one space: `key { ... }`.
        if (type1 == HoconElements.FIELD_KEY && type2 == HoconElements.OBJECT) {
            return Spacing.createSpacing(1, 1, 0, true, 0)
        }

        when (node.elementType) {
            HoconElements.OBJECT ->
                if (type1 == HoconTokens.LBRACE || type2 == HoconTokens.RBRACE) return expandWhenMultiline()

            HoconElements.ARRAY ->
                if (type1 == HoconTokens.LBRACKET || type2 == HoconTokens.RBRACKET ||
                    (isElement(type1) && isElement(type2))
                ) {
                    return expandWhenMultiline()
                }

            // Each entry of a nested object goes on its own line when the object spans multiple lines.
            // Only between real entries — never between an entry and its trailing comment.
            HoconElements.OBJECT_ENTRIES ->
                if (node.treeParent?.elementType == HoconElements.OBJECT && isEntry(type1) && isEntry(type2)) {
                    return expandWhenMultiline()
                }
        }

        return spacingBuilder.getSpacing(this, child1, child2)
    }

    private fun isEntry(type: IElementType?): Boolean =
        type == HoconElements.OBJECT_FIELD || type == HoconElements.INCLUDE

    private fun isElement(type: IElementType?): Boolean =
        type != null && type != HoconTokens.COMMA && !HoconTokenSets.COMMENTS.contains(type)

    /** Force a line break when this container spans multiple lines; keep it inline otherwise. */
    private fun expandWhenMultiline(): Spacing =
        Spacing.createDependentLFSpacing(0, 1, node.textRange, true, 1)

    override fun isLeaf(): Boolean = node.firstChildNode == null

    override fun getChildIndent(): Indent =
        when (node.elementType) {
            HoconElements.OBJECT, HoconElements.ARRAY -> Indent.getNormalIndent()
            else -> Indent.getNoneIndent()
        }
}
