package com.github.lppedd.cc.inspection

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.TemplateManagerListener
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.MessageBusConnection
import com.intellij.vcs.commit.message.BaseCommitMessageInspection

/**
 * @author Edoardo Luppi
 */
abstract class CommitBaseInspection : BaseCommitMessageInspection(), TemplateManagerListener, Disposable {
  @Volatile private var myMessageBus: MessageBusConnection? = null
  @Volatile private var myTemplateState: TemplateState? = null

  fun isTemplateActive(): Boolean =
    myTemplateState?.isFinished == false

  final override fun getGroupDisplayName(): String =
    super.getGroupDisplayName()

  final override fun getStaticDescription(): String? =
    super.getStaticDescription()

  final override fun checkFile(
      file: PsiFile,
      manager: InspectionManager,
      isOnTheFly: Boolean,
  ): Array<ProblemDescriptor?>? =
    super.checkFile(file, manager, isOnTheFly)

  final override fun buildVisitor(
      holder: ProblemsHolder,
      isOnTheFly: Boolean,
  ): PsiElementVisitor {
    subscribeForTemplateStarted(holder.project.messageBus)
    return super.buildVisitor(holder, isOnTheFly)
  }

  final override fun buildVisitor(
      holder: ProblemsHolder,
      isOnTheFly: Boolean,
      session: LocalInspectionToolSession,
  ): PsiElementVisitor =
    buildVisitor(holder, isOnTheFly)

  final override fun templateStarted(templateState: TemplateState) {
    myTemplateState = templateState
    registerDisposableWithEditor(templateState.editor)
  }

  abstract override fun checkFile(
      file: PsiFile,
      document: Document,
      manager: InspectionManager,
      isOnTheFly: Boolean,
  ): Array<ProblemDescriptor>

  private fun subscribeForTemplateStarted(messageBus: MessageBus) {
    if (myMessageBus == null) {
      myMessageBus = messageBus.connect().also {
        it.subscribe(TemplateManager.TEMPLATE_STARTED_TOPIC, this)
      }
    }
  }

  private fun registerDisposableWithEditor(editor: Editor) {
    when (editor) {
      is EditorWindow -> registerDisposableWithEditor(editor.delegate)
      is EditorImpl -> {
        val disposable = editor.disposable

        if (Disposer.findRegisteredObject(disposable, this) == null) {
          Disposer.register(disposable, this)
        }
      }
    }
  }

  override fun dispose() {
    myMessageBus?.disconnect()
    myMessageBus = null
  }

  @Suppress("exposed_super_class")
  object ConventionalCommitReformatQuickFix : ReformatCommitMessageQuickFix()
}
