package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.psi.CommitTypePsiElement
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
internal class CommitTypeDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(element: PsiElement, originalElement: PsiElement?) =
    if (element is CommitTypePsiElement) {
      val description = element.commitType.description ?: ""
      if (!description.isBlank()) description else null
    } else {
      null
    }

  override fun getDocumentationElementForLookupItem(
    psiManager: PsiManager?,
    obj: Any?,
    element: PsiElement?
  ) = if (obj is CommitLookupElement) {
    obj.psiElement
  } else {
    super.getDocumentationElementForLookupItem(psiManager, obj, element)
  }
}
