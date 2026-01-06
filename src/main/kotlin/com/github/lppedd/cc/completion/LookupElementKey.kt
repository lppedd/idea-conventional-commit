package com.github.lppedd.cc.completion

import com.github.lppedd.cc.api.CommitTokenProvider
import com.intellij.openapi.util.Key

/**
 * @author Edoardo Luppi
 */
internal object LookupElementKey {
  val Index: Key<Int> = Key.create("com.github.lppedd.cc.lookupElement.index")
  val Provider: Key<CommitTokenProvider> = Key.create("com.github.lppedd.cc.lookupElement.provider")
  val IsRecent: Key<Boolean> = Key.create("com.github.lppedd.cc.lookupElement.isRecent")
}
