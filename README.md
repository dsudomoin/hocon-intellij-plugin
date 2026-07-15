# HOCON

[![Build](https://github.com/dsudomoin/hocon-intellij-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/dsudomoin/hocon-intellij-plugin/actions/workflows/build.yml)

Language support for **HOCON** (Human-Optimized Config Object Notation — the Typesafe/Lightbend
Config format used by `application.conf`, Akka, Play, Kora and others) for IntelliJ-based IDEs.

The plugin is written from scratch in Kotlin with a hand-written lexer, parser and PSI, and depends
only on `com.intellij.modules.platform`, so it installs into any JetBrains IDE.

## Features

**Editing**
- File type for `.conf` and `.hocon` with syntax highlighting and a color settings page.
- Brace matching for `{}`, `[]` and `${}`; `#` line commenting; code folding for objects, arrays and
  multiline strings.
- Formatter and code style settings (2-space indent, spacing around separators/commas) that preserve
  value concatenations and multiline strings.
- Structure view and breadcrumbs, quote handling, auto-indent between braces, move statement up/down,
  and smart (extend) selection.

**Navigation & refactoring**
- Substitution resolution: `${path}` / `${?path}` navigate to key declarations, honoring HOCON merge
  semantics (a key may be declared many times) and `include`d files.
- `include "file"` / `include file("...")` navigate to the target file.
- Find usages, rename (updates every declaration of a path and all substitution usages), highlight
  usages, Copy Reference (full dotted path), and Go to Symbol across the project.

**Code assistance**
- Completion for substitution paths, `true`/`false`/`null` value keywords, and include-target paths.
- Inspections with quick fixes: unresolved substitution (make optional), unresolved include, and
  overridden (duplicate) keys.
- Quick documentation (hover / Ctrl+Q) showing a key's path, value and doc comment.
- Language injection into string values.

## Extending: the schema provider extension point

The plugin exposes an extension point so other plugins can teach it about a specific framework's
configuration keys — driving schema-aware completion, documentation and an "unknown key" inspection —
without any framework knowledge living in this plugin. The base plugin ships no schema providers.

```xml
<extensions defaultExtensionNs="io.github.dsudomoin.hocon">
    <schemaProvider implementation="com.example.MySchemaProvider"/>
</extensions>
```

```kotlin
class MySchemaProvider : HoconSchemaProvider {
    override fun getSchema(file: PsiFile): HoconSchema? =
        TreeHoconSchema(listOf(HoconKeySpec("server", "Object", children = listOf(
            HoconKeySpec("port", "Int", "The listening port"),
        ))))
}
```

## Requirements

- An IntelliJ-based IDE, build 251+ (2025.1 through 2026.x).
- JDK 21 to build from source.

## Building

| Task | Command |
|------|---------|
| Build the plugin | `./gradlew buildPlugin` |
| Run tests | `./gradlew test` |
| Verify compatibility | `./gradlew verifyPlugin` |
| Run a sandbox IDE | `./gradlew runIde` |

The distributable is produced under `build/distributions/`.

## Architecture

```
io.github.dsudomoin.hocon
├── lang/         Language, FileType, icons, PsiFile
├── lexer/        hand-written LexerBase + token types/sets
├── parser/       ParserDefinition, recursive-descent PsiParser, element types
├── psi/          PSI element classes, navigation API, manipulators, element factory
├── highlight/    syntax highlighter, colors, color settings page
├── annotator/    key highlighting annotator
├── editor/       brace matcher, commenter, quote handler, mover, word selection
├── folding/ formatting/ codestyle/ structure/     editing & structure features
├── semantics/    resolution engine (paths, includes, merge semantics)
├── ref/          substitution & include references
├── navigation/   find usages, rename, goto symbol, copy reference, read/write access
├── index/        project-wide key index
├── completion/   completion contributor
├── inspections/  inspections + quick fixes
├── documentation/ quick documentation provider
└── schema/       schemaProvider extension point + aggregator
```

## License

[MIT](LICENSE) © 2026 dsudomoin
