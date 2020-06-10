package com.github.lppedd.cc.api

/**
 * @author Edoardo Luppi
 */
interface CommitTokenProvider {
  fun getId(): String
  fun getPresentation(): ProviderPresentation
}
