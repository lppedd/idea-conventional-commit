package com.github.lppedd.cc.completion.weighers

import com.github.lppedd.cc.lookup.CommitScopeLookupElement

/**
 * @author Edoardo Luppi
 */
object CommitScopeElementWeigher :
  ConventionalCommitElementWeigher<CommitScopeLookupElement>(
    CommitScopeLookupElement::class.java,
    "commitScopeWeigher"
  )
