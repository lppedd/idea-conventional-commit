package com.github.lppedd.cc.completion.weighers

import com.github.lppedd.cc.lookup.CommitSubjectLookupElement

/**
 * @author Edoardo Luppi
 */
object CommitSubjectElementWeigher :
  ConventionalCommitElementWeigher<CommitSubjectLookupElement>(
    CommitSubjectLookupElement::class.java,
    "commitSubjectWeigher"
  )
