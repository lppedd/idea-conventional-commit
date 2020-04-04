package com.github.lppedd.cc.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiFile
import com.intellij.vcs.commit.message.BaseCommitMessageInspection

/**
 * @author Edoardo Luppi
 */
abstract class ConventionalCommitBaseInspection : BaseCommitMessageInspection() {
  final override fun getGroupDisplayName(): String = super.getGroupDisplayName()
  final override fun getStaticDescription(): String? = super.getStaticDescription()
  final override fun checkFile(
      file: PsiFile,
      manager: InspectionManager,
      isOnTheFly: Boolean,
  ): Array<ProblemDescriptor>? =
    super.checkFile(file, manager, isOnTheFly)

  abstract override fun checkFile(
      file: PsiFile,
      document: Document,
      manager: InspectionManager,
      isOnTheFly: Boolean,
  ): Array<ProblemDescriptor>

  @Suppress("EXPOSED_SUPER_CLASS")
  object ConventionalCommitReformatQuickFix : ReformatCommitMessageQuickFix()
}
