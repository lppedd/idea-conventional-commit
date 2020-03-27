package com.github.lppedd.cc.lookupElement

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
internal object CommitNoScopeLookupElement : LookupElement() {
  override fun getLookupString(): String = ""
  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.itemText = "No scope"
    presentation.isItemTextItalic = true
  }
}
