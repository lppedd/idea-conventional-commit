package com.github.lppedd.cc.lookup

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation

/**
 * @author Edoardo Luppi
 */
class NoScopeLookupElement : LookupElement() {
  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.itemText = "No scope"
    presentation.isItemTextItalic = true
  }

  override fun getLookupString() = ""
}
