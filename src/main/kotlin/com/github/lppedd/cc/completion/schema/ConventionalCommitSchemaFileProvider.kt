package com.github.lppedd.cc.completion.schema

import com.github.lppedd.cc.CC
import com.intellij.json.JsonFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType

/**
 * @author Edoardo Luppi
 */
internal class ConventionalCommitSchemaFileProvider : JsonSchemaFileProvider, DumbAware {
  override fun getName(): String =
    "Conventional Commit"

  override fun getSchemaFile(): VirtualFile? =
    JsonSchemaProviderFactory.getResourceFile(this::class.java, "/defaults/${CC.File.Schema}")

  override fun getSchemaType(): SchemaType =
    SchemaType.embeddedSchema

  override fun isAvailable(file: VirtualFile): Boolean =
    file.isValid && file.fileType is JsonFileType && CC.File.Defaults.equals(file.name, ignoreCase = true)
}
