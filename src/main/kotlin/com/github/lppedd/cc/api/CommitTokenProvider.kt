package com.github.lppedd.cc.api

import java.time.Duration

/**
 * @author Edoardo Luppi
 */
interface CommitTokenProvider {
  fun getId(): String
  fun getPresentation(): ProviderPresentation

  @JvmDefault
  fun timeout(): Duration = Duration.ofSeconds(1)
}
