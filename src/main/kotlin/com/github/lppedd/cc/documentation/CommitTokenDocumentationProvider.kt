package com.github.lppedd.cc.documentation

import com.github.lppedd.cc.lookupElement.CommitLookupElement
import com.github.lppedd.cc.psiElement.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * @author Edoardo Luppi
 */
private class CommitTokenDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
    val description = when (element) {
      !is CommitFakePsiElement -> null
      is CommitTypePsiElement -> element.commitType.description
      is CommitScopePsiElement -> element.commitScope.description
      is CommitBodyPsiElement -> element.commitBody.description
      is CommitFooterPsiElement -> element.commitFooter.description
      is CommitFooterTypePsiElement -> element.commitFooterType.description
      else -> null
    }

    return description?.ifBlank { null }
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
