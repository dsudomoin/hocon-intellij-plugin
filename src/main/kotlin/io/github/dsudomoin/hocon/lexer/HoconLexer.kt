package io.github.dsudomoin.hocon.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import io.github.dsudomoin.hocon.lexer.HoconTokenSets.isHoconWhitespace
import io.github.dsudomoin.hocon.lexer.HoconTokens.BAD_CHARACTER
import io.github.dsudomoin.hocon.lexer.HoconTokens.COLON
import io.github.dsudomoin.hocon.lexer.HoconTokens.COMMA
import io.github.dsudomoin.hocon.lexer.HoconTokens.DOLLAR
import io.github.dsudomoin.hocon.lexer.HoconTokens.DOUBLE_SLASH_COMMENT
import io.github.dsudomoin.hocon.lexer.HoconTokens.EQUALS
import io.github.dsudomoin.hocon.lexer.HoconTokens.HASH_COMMENT
import io.github.dsudomoin.hocon.lexer.HoconTokens.INLINE_WHITESPACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.LBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.LBRACKET
import io.github.dsudomoin.hocon.lexer.HoconTokens.LINE_BREAKING_WHITESPACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.LPAREN
import io.github.dsudomoin.hocon.lexer.HoconTokens.MULTILINE_STRING
import io.github.dsudomoin.hocon.lexer.HoconTokens.PERIOD
import io.github.dsudomoin.hocon.lexer.HoconTokens.PLUS_EQUALS
import io.github.dsudomoin.hocon.lexer.HoconTokens.QMARK
import io.github.dsudomoin.hocon.lexer.HoconTokens.QUOTED_STRING
import io.github.dsudomoin.hocon.lexer.HoconTokens.RBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.RBRACKET
import io.github.dsudomoin.hocon.lexer.HoconTokens.RPAREN
import io.github.dsudomoin.hocon.lexer.HoconTokens.SUB_LBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.SUB_RBRACE
import io.github.dsudomoin.hocon.lexer.HoconTokens.UNQUOTED_CHARS

/**
 * Рукописный лексер HOCON.
 *
 * Состояния нужны, чтобы отличать скобки подстановки `${ }` (SUB_LBRACE/SUB_RBRACE)
 * от скобок объекта, распознавать `${?` и корректно перезапускаться при инкрементальной
 * лексикализации. Точка `.` и скобки `(` `)` всегда отдельные токены — ключи/значения
 * пересобирает парсер.
 */
class HoconLexer : LexerBase() {
    private enum class St { INITIAL, VALUE, SUB_STARTING, SUB_STARTED, SUBSTITUTION }

    private var buf: CharSequence = ""
    private var end = 0
    private var stateBefore = St.INITIAL
    private var stateAfter = St.INITIAL
    private var tStart = 0
    private var tEnd = 0
    private var tType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        buf = buffer
        tStart = startOffset
        tEnd = startOffset
        end = endOffset
        stateBefore = St.entries[initialState]
        stateAfter = St.entries[initialState]
        tType = null
    }

    override fun getState(): Int = stateBefore.ordinal

    override fun getBufferSequence(): CharSequence = buf

    override fun getBufferEnd(): Int = end

    override fun getTokenStart(): Int = tStart

    override fun getTokenEnd(): Int = tEnd

    override fun getTokenType(): IElementType? {
        if (tType == null) advance()
        return tType
    }

    override fun advance() {
        tStart = tEnd
        if (tStart >= end) {
            stateBefore = St.INITIAL
            stateAfter = St.INITIAL
            tType = null
            return
        }
        val c = buf[tStart]
        when {
            c == '$' -> set(DOLLAR, 1, onDollar(stateAfter))
            c == '?' && stateAfter == St.SUB_STARTED -> set(QMARK, 1, St.SUBSTITUTION)
            c == '{' ->
                if (stateAfter == St.SUB_STARTING) set(SUB_LBRACE, 1, St.SUB_STARTED) else set(LBRACE, 1, St.INITIAL)

            c == '}' ->
                if (stateAfter == St.SUB_STARTED || stateAfter == St.SUBSTITUTION) {
                    set(SUB_RBRACE, 1, St.VALUE)
                } else {
                    set(RBRACE, 1, St.VALUE)
                }

            c == '[' -> set(LBRACKET, 1, St.INITIAL)
            c == ']' -> set(RBRACKET, 1, St.VALUE)
            c == '(' -> set(LPAREN, 1, St.INITIAL)
            c == ')' -> set(RPAREN, 1, St.VALUE)
            c == ':' -> set(COLON, 1, St.INITIAL)
            c == ',' -> set(COMMA, 1, St.INITIAL)
            c == '=' -> set(EQUALS, 1, St.INITIAL)
            c == '+' && containsAt(tStart, "+=") -> set(PLUS_EQUALS, 2, St.INITIAL)
            c == '.' -> set(PERIOD, 1, onContents(stateAfter))
            c == '#' -> set(HASH_COMMENT, lineEnd(tStart) - tStart, stateAfter)
            c == '/' && containsAt(tStart, "//") -> set(DOUBLE_SLASH_COMMENT, lineEnd(tStart) - tStart, stateAfter)
            c == '"' && isTripleQuoteAt(tStart) -> set(
                MULTILINE_STRING,
                multilineEnd() - tStart,
                onContents(stateAfter)
            )

            c == '"' -> set(QUOTED_STRING, quotedEnd() - tStart, onContents(stateAfter))
            isHoconWhitespace(c) -> {
                var i = tStart
                var nl = false
                while (i < end && isHoconWhitespace(buf[i])) {
                    if (buf[i].code == 10) nl = true
                    i++
                }
                set(if (nl) LINE_BREAKING_WHITESPACE else INLINE_WHITESPACE, i - tStart, onWhitespace(stateAfter, nl))
            }

            continuesUnquoted(tStart) -> {
                var i = tStart
                while (continuesUnquoted(i)) i++
                set(UNQUOTED_CHARS, i - tStart, onContents(stateAfter))
            }

            else -> set(BAD_CHARACTER, 1, stateAfter)
        }
    }

    private fun set(type: IElementType, len: Int, newState: St) {
        tEnd = tStart + len
        tType = type
        stateBefore = stateAfter
        stateAfter = newState
    }

    private fun onContents(s: St): St = when (s) {
        St.INITIAL, St.SUB_STARTING -> St.VALUE
        St.SUB_STARTED -> St.SUBSTITUTION
        else -> s
    }

    private fun onDollar(s: St): St = when (s) {
        St.INITIAL, St.VALUE -> St.SUB_STARTING
        St.SUB_STARTED -> St.SUBSTITUTION
        else -> s
    }

    private fun onWhitespace(s: St, nl: Boolean): St = when {
        nl -> St.INITIAL
        s == St.SUB_STARTING -> St.VALUE
        s == St.SUB_STARTED -> St.SUBSTITUTION
        else -> s
    }

    private fun containsAt(off: Int, s: String): Boolean {
        if (off + s.length > end) return false
        for (i in s.indices) if (buf[off + i] != s[i]) return false
        return true
    }

    private fun isTripleQuoteAt(i: Int): Boolean =
        i + 2 < end && buf[i] == '"' && buf[i + 1] == '"' && buf[i + 2] == '"'

    private fun lineEnd(from: Int): Int {
        var i = from
        while (i < end && buf[i].code != 10) i++
        return i
    }

    private fun quotedEnd(): Int {
        var i = tStart + 1
        var esc = false
        while (i < end) {
            val ch = buf[i]
            if (ch.code == 10) return i
            if (ch == '"' && !esc) return i + 1
            esc = ch.code == 92 && !esc
            i++
        }
        return i
    }

    private fun multilineEnd(): Int {
        var i = tStart + 3
        while (i < end) {
            if (buf[i] == '"') {
                var q = i
                while (q < end && buf[q] == '"') q++
                if (q - i >= 3) return q
                i = q
            } else {
                i++
            }
        }
        return end
    }

    private fun continuesUnquoted(i: Int): Boolean {
        if (i >= end) return false
        val ch = buf[i]
        return !isUnquotedSpecial(ch) && !isForbidden(ch) && !isHoconWhitespace(ch) &&
                (ch != '/' || i + 1 >= end || buf[i + 1] != '/')
    }

    private fun isUnquotedSpecial(c: Char): Boolean = c == '.' || c == '(' || c == ')'

    private fun isForbidden(c: Char): Boolean = when {
        c == '$' || c == '"' || c == '{' || c == '}' || c == '[' || c == ']' -> true
        c == ':' || c == '=' || c == ',' || c == '+' || c == '#' || c == '`' -> true
        c == '^' || c == '?' || c == '!' || c == '@' || c == '*' || c == '&' -> true
        c.code == 92 -> true
        else -> false
    }
}
