package com.github.lppedd.cc.configuration

/**
 * @author Edoardo Luppi
 */
internal sealed interface CoAuthorsResult {
  data class Success(val coAuthors: Set<String>) : CoAuthorsResult
  data class Failure(val message: String) : CoAuthorsResult
}
