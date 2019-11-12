package com.github.lppedd.cc.completion.weighers

import com.github.lppedd.cc.lookup.CommitTypeLookupElement

/**
 * @author Edoardo Luppi
 */
object CommitTypeElementWeigher :
  ConventionalCommitElementWeigher<CommitTypeLookupElement>(
    CommitTypeLookupElement::class.java,
    "commitTypeWeigher"
  )
