package com.github.lppedd.cc.lookup

import com.github.lppedd.cc.Helper
import com.github.lppedd.cc.completion.ConventionalCommitCompletionProvider
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import java.util.*
import kotlin.test.assertNotNull

/**
 * @author Edoardo Luppi
 */
class InitCommitTypeLookupElement(
  private val completionProvider: ConventionalCommitCompletionProvider,
  psiElement: CommitTypePsiElement
) : CommitTypeLookupElement(0, psiElement) {
  private val templateSettings = TemplateSettings.getInstance()

  override fun handleInsert(context: InsertionContext) {
    val template =
      templateSettings.getTemplateById("ConventionalCommit-cs") as TemplateImpl?
        ?: return

    WriteCommandAction.runWriteCommandAction(context.project) {
      context.document.setText("")
    }

    completionProvider.block()

    val predefinedValues: MutableMap<String, String> = HashMap()
    predefinedValues["TYPE"] = psiElement.commitTypeName

    val commitType = assertNotNull(template.variables[0])
    Helper.setFieldValue("mySkipOnStart", commitType, true)

    TemplateManager.getInstance(context.project).startTemplate(
      context.editor,
      template,
      true,
      predefinedValues,
      CCTemplateEditingListener(completionProvider)
    )
  }
}

class CCTemplateEditingListener(private val completionProvider: ConventionalCommitCompletionProvider) : TemplateEditingAdapter() {
  override fun templateFinished(template: Template, brokenOff: Boolean) {
    completionProvider.accept()
  }

  override fun templateCancelled(template: Template?) {
    completionProvider.accept()
  }

  override fun currentVariableChanged(templateState: TemplateState, template: Template?, oldIndex: Int, newIndex: Int) {
    if (oldIndex == 1 && newIndex == 2) {
      val segmentRange = templateState.getSegmentRange(1)

      if (segmentRange.isEmpty) {
        runWriteAction {
          val document = templateState.editor.document
          document.deleteString(segmentRange.startOffset - 1, segmentRange.endOffset + 1)
          PsiDocumentManager
            .getInstance(templateState.editor.project ?: return@runWriteAction)
            .commitDocument(document)
        }

        TemplateManager.getInstance(templateState.editor.project).startTemplate(
          templateState.editor,
          TemplateSettings.getInstance().getTemplateById("ConventionalCommit-subject"),
          true,
          emptyMap(),
          this
        )
      }
    }
  }
}
