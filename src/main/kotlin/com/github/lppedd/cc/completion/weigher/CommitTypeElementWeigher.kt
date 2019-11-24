package com.github.lppedd.cc.completion.weigher

import com.github.lppedd.cc.lookupElement.CommitTypeLookupElement

/**
 * @author Edoardo Luppi
 */
internal object CommitTypeElementWeigher :
  CommitElementWeigher<CommitTypeLookupElement>(
    CommitTypeLookupElement::class.java,
    "commitTypeWeigher"
  )
