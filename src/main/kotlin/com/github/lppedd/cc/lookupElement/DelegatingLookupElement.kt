package com.github.lppedd.cc.lookupElement

import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Edoardo Luppi
 */
internal sealed interface DelegatingLookupElement<out T : LookupElement> {
  fun getDelegate(): T
}
