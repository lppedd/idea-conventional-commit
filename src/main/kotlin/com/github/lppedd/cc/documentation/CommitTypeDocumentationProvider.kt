package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.lookup.ConventionalCommitLookupElement
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
class CommitTypeDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(element: PsiElement, originalElement: PsiElement?) =
    if (element !is CommitTypePsiElement) null
    else element.commitTypeDescription

  override fun getDocumentationElementForLookupItem(
    psiManager: PsiManager?,
    obj: Any?,
    element: PsiElement?
  ) =
    if (obj is ConventionalCommitLookupElement) obj.psiElement
    else super.getDocumentationElementForLookupItem(psiManager, obj, element)
}
