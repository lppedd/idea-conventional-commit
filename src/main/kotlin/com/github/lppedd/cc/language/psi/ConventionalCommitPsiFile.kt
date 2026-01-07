package com.github.lppedd.cc.language.psi

import com.github.lppedd.cc.language.ConventionalCommitFileType
import com.github.lppedd.cc.language.ConventionalCommitLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

/**
 * @author Edoardo Luppi
 */
public class ConventionalCommitPsiFile(
  viewProvider: FileViewProvider,
) : PsiFileBase(viewProvider, ConventionalCommitLanguage) {
  override fun getFileType(): FileType =
    ConventionalCommitFileType
}
