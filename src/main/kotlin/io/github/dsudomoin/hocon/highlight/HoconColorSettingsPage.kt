package io.github.dsudomoin.hocon.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import io.github.dsudomoin.hocon.lang.HoconIcons
import javax.swing.Icon

private val DESCRIPTORS =
    arrayOf(
        AttributesDescriptor("Key", HoconColors.KEY),
        AttributesDescriptor("String", HoconColors.STRING),
        AttributesDescriptor("Number", HoconColors.NUMBER),
        AttributesDescriptor("Keyword (true/false/null)", HoconColors.KEYWORD),
        AttributesDescriptor("Substitution", HoconColors.SUBSTITUTION),
        AttributesDescriptor("Substitution sign", HoconColors.SUBSTITUTION_SIGN),
        AttributesDescriptor("Braces", HoconColors.BRACES),
        AttributesDescriptor("Brackets", HoconColors.BRACKETS),
        AttributesDescriptor("Parentheses", HoconColors.PARENTHESES),
        AttributesDescriptor("Comma", HoconColors.COMMA),
        AttributesDescriptor("Key/value separator", HoconColors.SEPARATOR),
        AttributesDescriptor("Dot", HoconColors.DOT),
        AttributesDescriptor("Comment", HoconColors.COMMENT),
        AttributesDescriptor("Bad character", HoconColors.BAD_CHARACTER),
    )

private val DEMO =
    buildString {
        append("# HOCON demo\n")
        append("server {\n")
        append("  <key>host</key> = ").append('"').append("localhost").append('"').append("\n")
        append("  <key>port</key> = 8080\n")
        append("  <key>url</key> = ").append("$").append("{server.host}\n")
        append("  <key>tags</key> = [a, b, c]\n")
        append("  <key>enabled</key> = true\n")
        append("}\n")
    }

class HoconColorSettingsPage : ColorSettingsPage {
    override fun getIcon(): Icon = HoconIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = HoconSyntaxHighlighter()

    override fun getDemoText(): String = DEMO

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> =
        mapOf("key" to HoconColors.KEY)

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "HOCON"
}
