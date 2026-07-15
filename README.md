# HOCON

[![Build](https://github.com/dsudomoin/hocon-intellij-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/dsudomoin/hocon-intellij-plugin/actions/workflows/build.yml)

Language support for **HOCON** (Human-Optimized Config Object Notation — the Typesafe/Lightbend
Config format used by `application.conf`, Akka, Play, Kora and others) for IntelliJ-based IDEs.

The plugin is written from scratch in Kotlin with a hand-written lexer, parser and PSI, and depends
only on `com.intellij.modules.platform`, so it installs into any JetBrains IDE.

## Features

### Language & editing

- Recognizes `.conf` and `.hocon` and parses the **full HOCON grammar** — unquoted strings, value
  concatenation (`a = ${x} bar ${y}`), path expressions, `${...}` substitutions, `include`s and
  triple-quoted multiline strings — with a hand-written lexer and parser built for HOCON's
  context-sensitive corners.
- **Syntax highlighting** with a dedicated color settings page and bundled Default/Darcula schemes.
- **Brace matching** for `{}`, `[]` and `${}`, `#` line commenting, and **code folding** for objects,
  arrays and multiline strings.
- **Formatter & code style** — 2-space indent and configurable spacing around `:`/`=` separators and
  commas; value concatenations and multiline strings are deliberately left untouched.
- **Structure view** and **breadcrumbs** for the key hierarchy, plus auto-closing quotes, auto-indent
  between braces, **move statement up/down** (whole entries, including multiline) and HOCON-aware
  **extend selection**.

### Navigation & refactoring

- **Substitution resolution** — `${path}` / `${?path}` go to the key's declaration(s), honoring HOCON
  **merge semantics** (a key may be declared and overridden many times) and following `include`d files.
  Resolution also reaches `reference.conf` / `application.conf` bundled in library JARs on the classpath,
  matching `ConfigFactory.load` semantics.
- **Include navigation** — `include "file"`, `include file("...")` and `include classpath("...")` jump to
  the target file.
- **Find usages** and **Rename** (updates every declaration of a path and every substitution that reads
  it), plus **Highlight usages** (declarations as writes, substitutions as reads).
- **Go to Symbol** across the project (every key is indexed) and **Copy Reference** of the full dotted path.
- **Go to Next / Previous Definition** — jump between the repeated declarations of the same path, reusing
  the Go to Implementation / Go to Super shortcuts.
- **Cross-language references** (when the Java/Kotlin plugin is installed) — a HOCON path inside a Java,
  Kotlin or Scala string literal resolves to its key, and a fully-qualified class name inside a HOCON
  string resolves to the JVM class.

### Code assistance

- **Completion** — child keys inside a substitution (`${a.b.<caret>}`), the `true` / `false` / `null`
  value keywords, `include` target paths, and HOCON paths typed inside foreign-language string literals.
- **Inspections with quick fixes** — unresolved substitution (quick fix: make it optional), unresolved
  include, and an overridden key that a later declaration silently shadows.
- **Quick documentation** (hover / Ctrl+Q) — a key's full path, resolved value and doc comment.
- **Language injection** — string values are injection hosts, so any language (SQL, JSON, a regex, …) can
  be embedded and edited inside them.

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

The same schema also powers an optional **unknown-key** inspection (off by default until a provider is
registered). A companion `valueHighlighter` extension point lets a plugin colorize framework-specific
values — for example, enum constants — with their own text attributes.

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
