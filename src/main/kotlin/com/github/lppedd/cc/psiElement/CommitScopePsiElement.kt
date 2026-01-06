package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.CC
import com.intellij.openapi.project.Project
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
internal class CommitScopePsiElement(
    private val project: Project,
    private val presentableText: String,
) : CommitTokenPsiElement() {
  override fun getProject(): Project =
    project

  override fun getPresentableText(): String =
    presentableText

  override fun getIcon(unused: Boolean): Icon =
    CC.Icon.Token.Scope
}
