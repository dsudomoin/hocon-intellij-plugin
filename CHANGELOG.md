<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# HOCON Plugin Changelog

## [Unreleased]

### Added

- HOCON language support: file type for `.conf` and `.hocon`.
- Hand-written lexer and parser building a full PSI tree (objects, arrays,
  fields, path prefixes, substitutions `${...}`, includes, concatenations,
  literals).
- Syntax highlighting (strings, comments, braces/brackets/parentheses,
  substitutions, separators, numbers, bad characters) with a color settings page.
- PSI-based highlighting of keys and substitution keys.
- Brace matching for `{}`, `[]` and `${}`.
- Line commenting with `#`.
- Code folding for objects, arrays and multiline strings.
- Formatter with code style settings (2-space indent, spacing around key/value
  separators and commas); value concatenations and multiline strings are preserved.
- Structure view and breadcrumbs showing the key hierarchy.
- Quote handler (auto-closing quotes) and auto-indent between braces.
- Move statement up/down for object entries (moves multiline entries as a unit).
- Extend selection (smart selection) starting from keys.
- Substitution resolution: `${path}` and `${?path}` navigate to key declarations,
  honoring HOCON merge semantics (a key may be declared many times) and `include`d files.
- Include navigation: `include "file.conf"` / `include file("...")` resolve to the target file.
- Find usages, rename (updates every declaration of a path and all substitution usages),
  highlight usages (declarations as writes, substitutions as reads).
- Go to symbol across the project (indexed HOCON keys) and Copy Reference (full dotted path).
- Code completion: substitution paths (`${a.b.<caret>}` suggests child keys), `true`/`false`/`null`
  value keywords, and include-target file paths.
- Inspections with quick fixes: unresolved substitution (make optional), unresolved include,
  overridden (duplicate) key that never takes effect.
- Quick documentation (hover / Ctrl+Q) for keys and substitutions: full path, value, and doc comment.
- Language injection: string values are injection hosts, so any language can be injected into them.
- `schemaProvider` extension point: external plugins can contribute a key schema that powers
  schema-aware completion, documentation, and an "unknown key" inspection (the base plugin ships none).
