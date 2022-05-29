package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.CCBundle
import com.github.lppedd.cc.CCIcons
import com.intellij.openapi.project.Project
import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
internal class NoScopeCommitPsiElement(private val project: Project) : CommitTokenPsiElement() {
  override fun getProject(): Project =
    project

  override fun getPresentableText(): String =
    CCBundle["cc.completion.noScope"]

  override fun getIcon(unused: Boolean): Icon =
    CCIcons.Tokens.Scope
}
