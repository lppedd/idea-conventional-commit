package com.github.lppedd.cc.completion

import com.intellij.openapi.project.Project
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitSchemaProviderFactory : JsonSchemaProviderFactory {
  private val providers = listOf(ConventionalCommitSchemaFileProvider())

  override fun getProviders(project: Project): List<JsonSchemaFileProvider> =
    providers
}
