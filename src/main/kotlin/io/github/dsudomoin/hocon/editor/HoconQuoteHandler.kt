package io.github.dsudomoin.hocon.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import io.github.dsudomoin.hocon.lexer.HoconTokens

class HoconQuoteHandler :
    SimpleTokenSetQuoteHandler(HoconTokens.QUOTED_STRING, HoconTokens.MULTILINE_STRING)
