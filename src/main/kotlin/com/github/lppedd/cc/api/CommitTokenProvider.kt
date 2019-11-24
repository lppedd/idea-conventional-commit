package com.github.lppedd.cc.api

import javax.swing.Icon

/**
 * @author Edoardo Luppi
 */
interface CommitTokenProvider {
  fun getId(): String
  fun getPresentationName(): String
  fun getPresentationIcon(): Icon
}
