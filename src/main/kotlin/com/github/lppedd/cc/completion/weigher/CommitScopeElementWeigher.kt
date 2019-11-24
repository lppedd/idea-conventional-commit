package com.github.lppedd.cc.completion.weigher

import com.github.lppedd.cc.lookupElement.CommitScopeLookupElement

/**
 * @author Edoardo Luppi
 */
internal object CommitScopeElementWeigher :
  CommitElementWeigher<CommitScopeLookupElement>(
    CommitScopeLookupElement::class.java,
    "commitScopeWeigher"
  )
