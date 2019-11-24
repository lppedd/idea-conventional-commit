package com.github.lppedd.cc.completion.weigher

import com.github.lppedd.cc.lookupElement.CommitSubjectLookupElement

/**
 * @author Edoardo Luppi
 */
internal object CommitSubjectElementWeigher :
  CommitElementWeigher<CommitSubjectLookupElement>(
    CommitSubjectLookupElement::class.java,
    "commitSubjectWeigher"
  )
