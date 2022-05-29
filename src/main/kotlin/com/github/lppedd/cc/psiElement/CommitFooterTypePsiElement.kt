package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.CCIcons
import com.intellij.openapi.project.Project
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterTypePsiElement(
    private val project: Project,
    private val presentableText: String,
) : CommitTokenPsiElement() {
  override fun getProject(): Project =
    project

  override fun getPresentableText(): String =
    presentableText

  override fun getIcon(unused: Boolean): Icon =
    CCIcons.Tokens.Footer
}
