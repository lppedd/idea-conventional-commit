package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.psiElement.CommitScopePsiElement
import com.github.lppedd.cc.psiElement.CommitTypePsiElement
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
private class CommitTokenDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(
      element: PsiElement?,
      originalElement: PsiElement?,
  ) = when (element) {
    is CommitTypePsiElement -> element.commitType.description?.ifBlank { null }
    is CommitScopePsiElement -> element.commitScope.description?.ifBlank { null }
    else -> null
  }

  override fun getDocumentationElementForLookupItem(
      psiManager: PsiManager?,
      obj: Any?,
      element: PsiElement?,
  ) = if (obj is CommitLookupElement) {
    obj.psiElement
  } else {
    super.getDocumentationElementForLookupItem(psiManager, obj, element)
  }
}
