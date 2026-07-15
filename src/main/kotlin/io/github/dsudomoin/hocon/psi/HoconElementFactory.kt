package io.github.dsudomoin.hocon.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import io.github.dsudomoin.hocon.lang.HoconFile
import io.github.dsudomoin.hocon.lang.HoconFileType

/** Creates throwaway HOCON PSI fragments for manipulators / refactorings. */
object HoconElementFactory {
    fun createFile(project: Project, text: String): HoconFile =
        PsiFileFactory.getInstance(project).createFileFromText("dummy.conf", HoconFileType, text) as HoconFile

    /** [keySource] must already be valid key source (raw or quoted) — see [renderHoconKey]. */
    fun createFieldKey(project: Project, keySource: String): HFieldKey? =
        PsiTreeUtil.findChildOfType(createFile(project, "$keySource = 0"), HFieldKey::class.java)

    fun createSubstitutionKey(project: Project, keySource: String): HSubstitutionKey? =
        PsiTreeUtil.findChildOfType(
            createFile(project, "x = " + "$" + "{" + keySource + "}"),
            HSubstitutionKey::class.java,
        )

    fun createSubstitution(project: Project, pathText: String, optional: Boolean): HSubstitution? {
        val qmark = if (optional) "?" else ""
        return PsiTreeUtil.findChildOfType(
            createFile(project, "x = " + "$" + "{" + qmark + pathText + "}"),
            HSubstitution::class.java,
        )
    }

    /** [source] must include the surrounding quotes. */
    fun createStringValue(project: Project, source: String): HStringValue? =
        PsiTreeUtil.findChildOfType(createFile(project, "x = $source"), HStringValue::class.java)
}
