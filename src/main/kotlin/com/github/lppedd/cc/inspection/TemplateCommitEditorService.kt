package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.getTemplateState
import com.github.lppedd.cc.isCommitMessage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class TemplateCommitEditorService(private val project: Project) {
  @Volatile private var commitEditor: Editor? = null

  init {
    @Suppress("IncorrectParentDisposable")
    EditorFactory.getInstance().addEditorFactoryListener(MyEditorFactoryListener(), project)
  }

  fun isTemplateActive(): Boolean =
    commitEditor?.getTemplateState()?.isFinished == false

  private inner class MyEditorFactoryListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
      val editor = event.editor

      if (editor.project == project && editor.document.isCommitMessage()) {
        commitEditor = editor
      }
    }

    override fun editorReleased(event: EditorFactoryEvent) {
      if (event.editor == commitEditor) {
        commitEditor = null
      }
    }
  }
}
