package io.github.dsudomoin.hocon.parser

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import io.github.dsudomoin.hocon.lang.HoconLanguage

class HoconElementType(debugName: String) : IElementType(debugName, HoconLanguage)

object HoconElements {
    @JvmField
    val FILE = IFileElementType("HOCON_FILE", HoconLanguage)

    @JvmField
    val OBJECT = HoconElementType("OBJECT")

    @JvmField
    val OBJECT_ENTRIES = HoconElementType("OBJECT_ENTRIES")

    @JvmField
    val INCLUDE = HoconElementType("INCLUDE")

    @JvmField
    val INCLUDED = HoconElementType("INCLUDED")

    @JvmField
    val QUALIFIED_INCLUDED = HoconElementType("QUALIFIED_INCLUDED")

    @JvmField
    val OBJECT_FIELD = HoconElementType("OBJECT_FIELD")

    @JvmField
    val PREFIXED_FIELD = HoconElementType("PREFIXED_FIELD")

    @JvmField
    val VALUED_FIELD = HoconElementType("VALUED_FIELD")

    @JvmField
    val PATH = HoconElementType("PATH")

    @JvmField
    val FIELD_KEY = HoconElementType("FIELD_KEY")

    @JvmField
    val SUBSTITUTION_KEY = HoconElementType("SUBSTITUTION_KEY")

    @JvmField
    val KEY_PART = HoconElementType("KEY_PART")

    @JvmField
    val ARRAY = HoconElementType("ARRAY")

    @JvmField
    val SUBSTITUTION = HoconElementType("SUBSTITUTION")

    @JvmField
    val CONCATENATION = HoconElementType("CONCATENATION")

    @JvmField
    val UNQUOTED_STRING = HoconElementType("UNQUOTED_STRING")

    @JvmField
    val STRING_VALUE = HoconElementType("STRING_VALUE")

    @JvmField
    val INCLUDE_TARGET = HoconElementType("INCLUDE_TARGET")

    @JvmField
    val NUMBER = HoconElementType("NUMBER")

    @JvmField
    val NULL = HoconElementType("NULL")

    @JvmField
    val BOOLEAN = HoconElementType("BOOLEAN")
}
