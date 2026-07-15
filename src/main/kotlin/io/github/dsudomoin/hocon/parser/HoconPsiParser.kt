package io.github.dsudomoin.hocon.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.ARRAY_ELEMENTS_ENDING
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.EMPTY
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.KEY_ENDING
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.KEY_VALUE_SEPARATOR
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.OBJECT_ENTRY_START
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.PATH_ENDING
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.STRING_LITERAL
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.SUBSTITUTION_PATH_START
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.VALUE_ENDING
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.VALUE_START
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.VALUE_UNQUOTED_CHARS
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.WHITESPACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.COMMA
import io.github.dsudomoin.hocon.lexer.HoconTokens.DOLLAR
import io.github.dsudomoin.hocon.lexer.HoconTokens.LBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.LBRACKET
import io.github.dsudomoin.hocon.lexer.HoconTokens.LINE_BREAKING_WHITESPACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.LPAREN
import io.github.dsudomoin.hocon.lexer.HoconTokens.MULTILINE_STRING
import io.github.dsudomoin.hocon.lexer.HoconTokens.PERIOD
import io.github.dsudomoin.hocon.lexer.HoconTokens.QUOTED_STRING
import io.github.dsudomoin.hocon.lexer.HoconTokens.RBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.RBRACKET
import io.github.dsudomoin.hocon.lexer.HoconTokens.RPAREN
import io.github.dsudomoin.hocon.lexer.HoconTokens.SUB_LBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.SUB_RBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.UNQUOTED_CHARS
import io.github.dsudomoin.hocon.lexer.Matcher
import io.github.dsudomoin.hocon.lexer.m
import io.github.dsudomoin.hocon.parser.HoconElements.ARRAY
import io.github.dsudomoin.hocon.parser.HoconElements.BOOLEAN
import io.github.dsudomoin.hocon.parser.HoconElements.CONCATENATION
import io.github.dsudomoin.hocon.parser.HoconElements.FIELD_KEY
import io.github.dsudomoin.hocon.parser.HoconElements.INCLUDE
import io.github.dsudomoin.hocon.parser.HoconElements.INCLUDED
import io.github.dsudomoin.hocon.parser.HoconElements.INCLUDE_TARGET
import io.github.dsudomoin.hocon.parser.HoconElements.KEY_PART
import io.github.dsudomoin.hocon.parser.HoconElements.NULL
import io.github.dsudomoin.hocon.parser.HoconElements.NUMBER
import io.github.dsudomoin.hocon.parser.HoconElements.OBJECT
import io.github.dsudomoin.hocon.parser.HoconElements.OBJECT_ENTRIES
import io.github.dsudomoin.hocon.parser.HoconElements.OBJECT_FIELD
import io.github.dsudomoin.hocon.parser.HoconElements.PATH
import io.github.dsudomoin.hocon.parser.HoconElements.PREFIXED_FIELD
import io.github.dsudomoin.hocon.parser.HoconElements.QUALIFIED_INCLUDED
import io.github.dsudomoin.hocon.parser.HoconElements.STRING_VALUE
import io.github.dsudomoin.hocon.parser.HoconElements.SUBSTITUTION
import io.github.dsudomoin.hocon.parser.HoconElements.SUBSTITUTION_KEY
import io.github.dsudomoin.hocon.parser.HoconElements.UNQUOTED_STRING
import io.github.dsudomoin.hocon.parser.HoconElements.VALUED_FIELD

private const val KW_INCLUDE = "include"
private const val KW_REQUIRED = "required"
private const val KW_URL = "url"
private const val KW_FILE = "file"
private const val KW_CLASSPATH = "classpath"
private const val KW_NULL = "null"
private const val KW_TRUE = "true"
private const val KW_FALSE = "false"
private val LOCATION_MODIFIERS = listOf(KW_URL, KW_FILE, KW_CLASSPATH)
private val INTEGER = Regex("-?(0|[1-9][0-9]*)")
private val DECIMAL = Regex("([0-9]+)([eE]([+-])?[0-9]+)?")

class HoconPsiParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val file = builder.mark()
        Parsing(builder).parseFile()
        file.done(root)
        return builder.treeBuilt
    }
}

private class Parsing(val b: PsiBuilder) {
    private var newLineSuppressedIndex = 0

    private fun newLinesBefore(): Boolean =
        b.rawTokenIndex() > newLineSuppressedIndex && b.rawLookup(-1) == LINE_BREAKING_WHITESPACE

    private fun suppressNewLine() {
        newLineSuppressedIndex = b.rawTokenIndex()
    }

    private fun matches(matcher: Matcher): Boolean =
        (matcher.tokens.contains(b.tokenType) && (!matcher.requireNoNewLine || !newLinesBefore())) ||
                (matcher.matchNewLine && newLinesBefore()) ||
                (matcher.matchEof && b.eof())

    private fun matchesUnquoted(s: String): Boolean = matches(UNQUOTED_CHARS.m()) && b.tokenText == s

    private fun matchesUnquoted(p: Regex): Boolean = matches(UNQUOTED_CHARS.m()) && p.matches(b.tokenText ?: "")

    private fun pass(matcher: Matcher): Boolean {
        val r = matches(matcher)
        if (r && (!matcher.matchNewLine || !newLinesBefore()) && (!matcher.matchEof || !b.eof())) {
            b.advanceLexer()
        }
        return r
    }

    private fun errorUntil(matcher: Matcher, msg: String, onlyNonEmpty: Boolean = false) {
        if (!onlyNonEmpty || !matches(matcher)) {
            val mk = b.mark()
            while (!matches(matcher)) b.advanceLexer()
            mk.error(msg)
        }
    }

    private fun tokenError(msg: String) {
        val mk = b.mark()
        b.advanceLexer()
        mk.error(msg)
    }

    fun parseFile() {
        when {
            matches(LBRACE.m()) -> parseObject()
            matches(LBRACKET.m()) -> parseArray()
            else -> parseObjectEntries(insideObject = false)
        }
        errorUntil(EMPTY.m().orEof(), "expected end of file", onlyNonEmpty = true)
    }

    private fun parseStringLiteral(type: HoconElementType) {
        val mk = b.mark()
        val txt = b.tokenText ?: ""
        val tt = b.tokenType
        val unclosedQuoted = tt == QUOTED_STRING && !isProperlyClosedQuoted(txt)
        val unclosedMultiline = tt == MULTILINE_STRING && !endsWithTripleQuote(txt)
        b.advanceLexer()
        if (unclosedQuoted) {
            b.error("unclosed quoted string")
        } else if (unclosedMultiline) {
            b.error("unclosed multiline string")
        }
        mk.done(type)
    }

    private fun isProperlyClosedQuoted(s: String): Boolean {
        if (s.length < 2 || s[s.length - 1] != '"') return false
        var i = s.length - 2
        var backslashes = 0
        while (i >= 0 && s[i].code == 92) {
            backslashes++
            i--
        }
        return backslashes % 2 == 0
    }

    private fun endsWithTripleQuote(s: String): Boolean =
        s.length >= 6 && s[s.length - 1] == '"' && s[s.length - 2] == '"' && s[s.length - 3] == '"'

    private fun parseObject() {
        val mk = b.mark()
        b.advanceLexer()
        parseObjectEntries(insideObject = true)
        if (!pass(RBRACE.m())) b.error("expected '}'")
        mk.done(OBJECT)
    }

    private fun parseObjectEntries(insideObject: Boolean) {
        val mk = b.mark()
        while (!matches(RBRACE.m().orEof())) {
            if (matches(OBJECT_ENTRY_START.m())) {
                parseObjectEntry()
                pass(COMMA.m())
            } else {
                tokenError("expected object field" + (if (insideObject) ", include or '}'" else " or include"))
            }
        }
        mk.done(OBJECT_ENTRIES)
    }

    private fun parseObjectEntry() {
        if (matchesUnquoted(KW_INCLUDE)) parseInclude() else parseObjectField()
        errorUntil(VALUE_ENDING.m().orNewLineOrEof(), "unexpected token", onlyNonEmpty = true)
    }

    private fun parseInclude() {
        val mk = b.mark()
        b.advanceLexer()
        parseIncluded()
        mk.done(INCLUDE)
    }

    private fun parseIncluded() {
        val mk = b.mark()
        if (matchesUnquoted(KW_REQUIRED)) {
            b.advanceLexer()
            if (matches(LPAREN.m()) && !WHITESPACE.contains(b.rawLookup(-1))) {
                b.advanceLexer()
                parseQualifiedIncluded()
                if (matches(RPAREN.m())) {
                    b.advanceLexer()
                } else {
                    errorUntil(VALUE_ENDING.m().orNewLineOrEof(), "expected ')'")
                }
            } else {
                errorUntil(VALUE_ENDING.m().orNewLineOrEof(), "expected '(' immediately after 'required'")
            }
        } else {
            parseQualifiedIncluded()
        }
        mk.done(INCLUDED)
    }

    private fun parseQualifiedIncluded() {
        val mk = b.mark()
        if (matches(QUOTED_STRING.m())) {
            parseStringLiteral(INCLUDE_TARGET)
        } else if (LOCATION_MODIFIERS.any { matchesUnquoted(it) }) {
            val qualifier = b.tokenText
            b.advanceLexer()
            if (matches(LPAREN.m()) && !WHITESPACE.contains(b.rawLookup(-1))) {
                b.advanceLexer()
                if (matches(QUOTED_STRING.m())) {
                    parseStringLiteral(INCLUDE_TARGET)
                    if (matches(RPAREN.m())) {
                        b.advanceLexer()
                    } else {
                        errorUntil(VALUE_ENDING.m().orNewLineOrEof(), "expected ')'")
                    }
                } else {
                    errorUntil(VALUE_ENDING.m().orNewLineOrEof(), "expected quoted string")
                }
            } else {
                errorUntil(VALUE_ENDING.m().orNewLineOrEof(), "expected '(' immediately after '$qualifier'")
            }
        } else {
            errorUntil(
                VALUE_ENDING.m().orNewLineOrEof(),
                "expected quoted string, optionally wrapped in url(...), file(...) or classpath(...)",
            )
        }
        mk.done(QUALIFIED_INCLUDED)
    }

    private fun parseObjectField() {
        val mk = b.mark()
        parseKeyedField(first = true)
        mk.done(OBJECT_FIELD)
    }

    private fun parseKeyedField(first: Boolean) {
        if (first) suppressNewLine()
        val mk = b.mark()
        tryParseKey(substitution = false)
        if (pass(PERIOD.m().noNewLine())) {
            parseKeyedField(first = false)
            mk.done(PREFIXED_FIELD)
        } else {
            if (matches(LBRACE.m())) {
                parseObject()
            } else if (pass(KEY_VALUE_SEPARATOR.m())) {
                if (matches(VALUE_START.m())) {
                    parseValue()
                } else {
                    errorUntil(VALUE_ENDING.m().orNewLineOrEof(), "expected value for object field")
                }
            } else {
                errorUntil(VALUE_ENDING.m().orNewLineOrEof(), "expected ':', '=', '+=' or object")
            }
            mk.done(VALUED_FIELD)
        }
    }

    private fun parsePath(prefix: PsiBuilder.Marker? = null) {
        val first = prefix == null
        if (first) suppressNewLine()
        if (!matches(PATH_ENDING.m().orNewLineOrEof())) {
            if (!first) pass(PERIOD.m().noNewLine())
            val mk = prefix?.precede() ?: b.mark()
            tryParseKey(substitution = true)
            mk.done(PATH)
            parsePath(mk)
        }
    }

    private fun tryParseKey(substitution: Boolean) {
        if (!matches(KEY_ENDING.m().orNewLineOrEof())) {
            parseKey(substitution)
        } else {
            b.error("expected key (use quoted \"\" if you want empty key)")
        }
    }

    private fun parseKey(substitution: Boolean) {
        val mk = b.mark()
        suppressNewLine()
        while (!matches(KEY_ENDING.m().orNewLineOrEof())) {
            when {
                matches(UNQUOTED_CHARS.m()) -> parseUnquotedString(KEY_PART, UNQUOTED_CHARS.m().noNewLine())
                matches(STRING_LITERAL.m()) -> parseStringLiteral(KEY_PART)
                else -> tokenError("key must be a concatenation of unquoted, quoted or multiline strings")
            }
        }
        mk.done(if (substitution) SUBSTITUTION_KEY else FIELD_KEY)
    }

    private fun parseUnquotedString(type: HoconElementType, matcher: Matcher) {
        val outer = b.mark()
        val inner = b.mark()
        suppressNewLine()
        while (matches(matcher)) b.advanceLexer()
        inner.done(UNQUOTED_STRING)
        outer.done(type)
    }

    private fun parseValue() {
        val ending = VALUE_ENDING.m().orNewLineOrEof()

        fun tryParse(code: () -> Boolean, element: HoconElementType): Boolean {
            val mk = b.mark()
            return if (code()) {
                mk.done(element)
                true
            } else {
                mk.rollbackTo()
                false
            }
        }

        fun passKeyword(kw: String): Boolean =
            if (matchesUnquoted(kw)) {
                b.advanceLexer()
                true
            } else {
                false
            }

        suppressNewLine()
        val nullOk = tryParse({ passKeyword(KW_NULL) && matches(ending) }, NULL)
        val boolOk = nullOk || tryParse({ (passKeyword(KW_TRUE) || passKeyword(KW_FALSE)) && matches(ending) }, BOOLEAN)
        val numOk = boolOk || tryParse({ passNumber() && matches(ending) }, NUMBER)
        if (!nullOk && !boolOk && !numOk) {
            val mk = b.mark()
            var parts = 0
            while (!matches(ending)) {
                when {
                    matches(LBRACE.m()) -> parseObject()
                    matches(LBRACKET.m()) -> parseArray()
                    matches(DOLLAR.m()) && b.lookAhead(1) == SUB_LBRACE -> parseSubstitution()
                    matches(VALUE_UNQUOTED_CHARS.m()) ->
                        parseUnquotedString(STRING_VALUE, VALUE_UNQUOTED_CHARS.m().noNewLine())

                    matches(STRING_LITERAL.m()) -> parseStringLiteral(STRING_VALUE)
                    else -> tokenError("forbidden unquoted character")
                }
                parts++
            }
            if (parts > 1) mk.done(CONCATENATION) else mk.drop()
        }
    }

    private fun passNumber(): Boolean {
        if (!matchesUnquoted(INTEGER)) return false
        val startIdx = b.rawTokenIndex()
        val sb = StringBuilder(b.tokenText ?: "")
        b.advanceLexer()
        val gotPeriod = matches(PERIOD.m())
        val periodOk = gotPeriod && b.rawTokenIndex() == startIdx + 1
        if (gotPeriod) {
            sb.append(b.tokenText)
            b.advanceLexer()
        }
        val gotDecimal = gotPeriod && matchesUnquoted(DECIMAL)
        val decimalOk = gotDecimal && b.rawTokenIndex() == startIdx + 2
        if (gotDecimal) {
            sb.append(b.tokenText)
            b.advanceLexer()
        }
        val valid =
            try {
                if (gotPeriod) sb.toString().toDouble() else sb.toString().toLong()
                true
            } catch (_: NumberFormatException) {
                false
            }
        return (!gotPeriod || periodOk) && (!gotDecimal || decimalOk) && valid
    }

    private fun parseArray() {
        val mk = b.mark()
        b.advanceLexer()
        while (!matches(ARRAY_ELEMENTS_ENDING.m().orEof())) {
            if (matches(VALUE_START.m())) {
                parseValue()
                pass(COMMA.m())
            } else {
                tokenError("expected array element or ']'")
            }
        }
        if (!pass(RBRACKET.m())) b.error("expected ']'")
        mk.done(ARRAY)
    }

    private fun parseSubstitution() {
        val mk = b.mark()
        b.advanceLexer()
        b.advanceLexer()
        pass(io.github.dsudomoin.hocon.lexer.HoconTokens.QMARK.m())
        if (matches(SUBSTITUTION_PATH_START.m().noNewLine())) {
            parsePath()
            if (!pass(SUB_RBRACE.m())) b.error("expected '}'")
        } else {
            errorUntil(PATH_ENDING.m().orNewLineOrEof(), "expected path expression")
        }
        pass(SUB_RBRACE.m())
        mk.done(SUBSTITUTION)
    }
}
