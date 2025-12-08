package com.github.lppedd.cc.vcs.commitbuilder

import com.github.lppedd.cc.moveCaretToOffset
import com.github.lppedd.cc.removeSelection
import com.github.lppedd.cc.selectAll
import com.github.lppedd.cc.ui.FixedEditorTextFieldBorder
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.util.textCompletion.TextFieldWithCompletion
import java.awt.Dimension
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

/**
 * @author Edoardo Luppi
 */
internal class CommitTokenTextField(
    project: Project,
    provider: CommitTokenTextCompletionProvider,
    private val lines: Int = 1,
) : TextFieldWithCompletion(project, provider, "", lines == 1, true, false) {
  private var doSelectAll = true

  init {
    addMouseListener(object : MouseAdapter() {
      override fun mousePressed(e: MouseEvent?) {
        doSelectAll = false
      }
    })

    addFocusListener(object : FocusListener {
      override fun focusGained(event: FocusEvent) {
        editor?.let {
          if (doSelectAll) {
            it.selectAll()
            it.moveCaretToOffset(it.document.textLength)
          } else {
            it.removeSelection(true)
          }
        }

        doSelectAll = true
      }

      override fun focusLost(event: FocusEvent) {
        // If the field lost focus in favor of a completion Lookup, it means when
        // that Lookup will be closed, we don't have to select all the text.
        // So setting this field to 'false' prevents it in 'focusGained'.
        doSelectAll = LookupManager.getActiveLookup(editor) == null

        if (doSelectAll) {
          editor?.removeSelection(true)
        }
      }
    })
  }

  override fun updateBorder(editor: EditorEx) {
    setupBorder(editor)
  }

  override fun getFocusTarget(): JComponent {
    return super.getFocusTarget().also {
      it.isFocusable = isEnabled
    }
  }

  override fun createEditor(): EditorEx {
    val editor = super.createEditor()
    val scrollPane = editor.scrollPane

    if (!editor.isOneLineMode) {
      editor.settings.isUseSoftWraps = true
      editor.setVerticalScrollbarVisible(true)

      val insets = scrollPane.border.getBorderInsets(scrollPane)
      val editorHeight = editor.lineHeight * lines + insets.top + insets.bottom
      scrollPane.preferredSize = Dimension(Int.MAX_VALUE, editorHeight)
      scrollPane.minimumSize = Dimension(Int.MAX_VALUE, editorHeight)
    }

    editor.setShowPlaceholderWhenFocused(true)
    editor.setBorder(FixedEditorTextFieldBorder(scrollPane.border))
    return editor
  }
}
