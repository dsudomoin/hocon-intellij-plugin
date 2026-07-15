package io.github.dsudomoin.hocon.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import io.github.dsudomoin.hocon.lexer.HoconTokens

/**
 * Maps an injected language fragment onto the content of a HOCON string value (the text inside the quotes).
 * Escapes are passed through verbatim, which is adequate for the common injection cases (SQL, JSON, regex).
 */
class HoconLiteralTextEscaper(host: HStringValue) : LiteralTextEscaper<HStringValue>(host) {
    override fun getRelevantTextRange(): TextRange =
        ElementManipulators.getManipulator(myHost)?.getRangeInElement(myHost) ?: TextRange(0, myHost.textLength)

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        outChars.append(myHost.text, rangeInsideHost.startOffset, rangeInsideHost.endOffset)
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int =
        rangeInsideHost.startOffset + offsetInDecoded

    override fun isOneLine(): Boolean = myHost.node.firstChildNode?.elementType == HoconTokens.QUOTED_STRING
}
