package com.github.lppedd.cc.language

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.*

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
  private val highlighters: MutableMap<Project?, SyntaxHighlighter> = WeakHashMap(8)

  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
    highlighters.computeIfAbsent(project, ::ConventionalCommitSyntaxHighlighter)
}
