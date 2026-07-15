package io.github.dsudomoin.hocon.formatting

import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet
import io.github.dsudomoin.hocon.lang.HoconLanguage
import io.github.dsudomoin.hocon.lexer.HoconTokens

class HoconFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val root = HoconBlock(formattingContext.node, spacing(settings), Indent.getNoneIndent())
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            root,
            settings,
        )
    }

    private fun spacing(settings: CodeStyleSettings): SpacingBuilder =
        SpacingBuilder(settings, HoconLanguage)
            // ':' hugs the key (JSON/HOCON convention): no space before, one after.
            .before(HoconTokens.COLON).spaces(0)
            .after(HoconTokens.COLON).spaces(1)
            // '=' and '+=' get one space on each side.
            .around(TokenSet.create(HoconTokens.EQUALS, HoconTokens.PLUS_EQUALS)).spaces(1)
            .before(HoconTokens.COMMA).spaces(0)
            .after(HoconTokens.COMMA).spaces(1)
}
