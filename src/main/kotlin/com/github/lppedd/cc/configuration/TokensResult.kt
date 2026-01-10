package com.github.lppedd.cc.configuration

import com.github.erosb.jsonsKema.ValidationFailure
import com.github.lppedd.cc.configuration.CCTokensService.TokensModel

/**
 * @author Edoardo Luppi
 */
internal sealed interface TokensResult {
  data class Success(val tokens: TokensModel) : TokensResult
  data class FileError(val message: String) : TokensResult
  data class SchemaError(val failure: ValidationFailure) : TokensResult
}
