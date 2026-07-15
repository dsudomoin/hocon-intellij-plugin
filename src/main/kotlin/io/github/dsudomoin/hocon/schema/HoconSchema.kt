package io.github.dsudomoin.hocon.schema

/**
 * Specification of a single known key, supplied by a [HoconSchemaProvider]. Drives schema-aware completion,
 * documentation and the "unknown key" inspection.
 */
data class HoconKeySpec(
    val name: String,
    val type: String? = null,
    val description: String? = null,
    val default: String? = null,
    /** Allowed values for an enum-typed key; empty otherwise. Drives value completion. */
    val enumValues: List<String> = emptyList(),
    val required: Boolean = false,
    val children: List<HoconKeySpec> = emptyList(),
)

/** A resolved schema for a HOCON file: the known keys available at each dotted path. */
interface HoconSchema {
    /** Key specs valid directly under [path] (an empty path means the config root). */
    fun keysAt(path: List<String>): List<HoconKeySpec>
}

/**
 * Convenience [HoconSchema] backed by a tree of root [HoconKeySpec]s: [keysAt] walks the tree by name.
 */
class TreeHoconSchema(private val roots: List<HoconKeySpec>) : HoconSchema {
    override fun keysAt(path: List<String>): List<HoconKeySpec> {
        var level = roots
        for (segment in path) {
            level = level.firstOrNull { it.name == segment }?.children ?: return emptyList()
        }
        return level
    }
}
