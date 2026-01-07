package com.github.lppedd.cc.liveTemplate

import com.github.lppedd.cc.invokeLaterOnEdt
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionPhase
import com.intellij.codeInsight.completion.CompletionType.BASIC
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl
import com.intellij.codeInsight.template.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiUtilBase

/**
 * @see com.intellij.codeInsight.template.macro.BaseCompleteMacro
 * @author Edoardo Luppi
 */
internal abstract class CommitBaseCompleteMacro : Macro() {
  override fun getPresentableName(): String =
    "$name()"

  override fun getDefaultValue(): String =
    "a"

  override fun calculateResult(params: Array<Expression>, context: ExpressionContext): Result =
    InvokeActionResult { invokeCompletion(context) }

  private fun invokeCompletion(context: ExpressionContext) {
    val project = context.project
    val editor = context.editor ?: return
    val psiFile = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return

    invokeLaterOnEdt {
      if (
        project.isDisposed ||
        editor.isDisposed ||
        !psiFile.isValid ||
        CompletionServiceImpl.completionService.currentCompletion != null
      ) {
        return@invokeLaterOnEdt
      }

      val command = Runnable {
        @Suppress("UnstableApiUsage")
        CompletionServiceImpl.setCompletionPhase(CompletionPhase.NoCompletion)
        invokeCompletionHandler(project, editor)
      }

      CommandProcessor.getInstance().executeCommand(project, command, "", null)
    }
  }

  private fun invokeCompletionHandler(project: Project, editor: Editor) {
    val invokedExplicitly = ApplicationManager.getApplication().isUnitTestMode
    CodeCompletionHandlerBase
      .createHandler(BASIC, invokedExplicitly, !invokedExplicitly, true)
      .invokeCompletion(project, editor, -1)
  }
}
