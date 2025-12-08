package com.github.lppedd.cc.configuration

import com.github.erosb.jsonsKema.ValidationFailure

/**
 * @author Edoardo Luppi
 */
internal class SchemaValidationException(failure: ValidationFailure) : RuntimeException("$failure")
