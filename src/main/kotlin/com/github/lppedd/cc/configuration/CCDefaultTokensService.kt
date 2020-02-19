package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.getResourceAsStream
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import org.everit.json.schema.Schema
import org.everit.json.schema.Validator
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Manages default commit types and scopes.
 *
 * @author Edoardo Luppi
 */
internal class CCDefaultTokensService(private val project: Project) {
  companion object {
    fun getInstance(project: Project): CCDefaultTokensService =
      ServiceManager.getService(project, CCDefaultTokensService::class.java)
  }

  /** JSON Schema used to validate the default commit types and scopes JSON file. */
  private val defaultsSchema by lazy(::loadDefaultsSchema)

  /** Built-in default commit types and scopes. */
  private val builtInDefaultTokens by lazy(::readBuiltInDefaults)

  fun getDefaultsFromCustomFile(path: String? = null): Map<String, JsonCommitType> {
    val filePath = path ?: findDefaultFilePathFromProjectRoot()
    return if (filePath != null) {
      readDefaultsFromFile(filePath)
    } else {
      builtInDefaultTokens
    }
  }

  /**
   * Returns the built-in commit types and scopes.
   */
  fun getBuiltInDefaults(): Map<String, JsonCommitType> = builtInDefaultTokens.toMap()

  /**
   * Validates a file via the inputted absolute path,
   * or the implicit file in project's root if `null`, using the JSON Schema.
   */
  fun validateDefaultsFile(path: String? = null) {
    val filePath = path ?: findDefaultFilePathFromProjectRoot() ?: return
    val reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)
    reader.use {
      validateJsonWithSchema(JSONObject(JSONTokener(reader)))
    }
  }

  /**
   * Returns the full path of the default tokens file located
   * in the project root directory, or `null`.
   */
  private fun findDefaultFilePathFromProjectRoot(): String? =
    project.guessProjectDir()
      ?.findChild(CCConstants.DEFAULT_FILE)
      ?.path

  /**
   * Reads default commit types and scopes from a file in FS via its absolute path.
   */
  private fun readDefaultsFromFile(filePath: String): Map<String, JsonCommitType> {
    val reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)
    return reader.use(::readFile)
  }

  /**
   * Reads default commit types and scopes from the built-in file.
   */
  private fun readBuiltInDefaults(): Map<String, JsonCommitType> {
    val inputStream = getResourceAsStream("/defaults/${CCConstants.DEFAULT_FILE}")
    val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
    return reader.use(::readFile)
  }

  /**
   * Reads a file using the inputted `Reader` and transforms the JSON content
   * in a map of commit types and their associated scopes.
   *
   * **Note that the `Reader` must be closed by the caller.**
   */
  private fun readFile(reader: Reader): Map<String, JsonCommitType> {
    val rootJsonObject = JSONObject(JSONTokener(reader))

    // If the inputted JSON isn't valid an exception is thrown.
    // The exception contains the validation errors which can be used to notify the user
    validateJsonWithSchema(rootJsonObject)

    val jsonTypes = rootJsonObject.getJSONObject("types")
    val jsonCommonScopes = rootJsonObject.optJSONObject("commonScopes")

    // Common scopes must be added to each type's scopes
    val commonScopes = buildScopes(jsonCommonScopes)

    return jsonTypes.keySet()
      .associateWith {
        val type = jsonTypes.getJSONObject(it)
        val jsonTypeScopes = type.optJSONObject("scopes")
        JsonCommitType(
          type.optString("description"),
          commonScopes.plus(buildScopes(jsonTypeScopes))
        )
      }
  }

  /**
   * Validate the JSON object against a JSON Schema.
   */
  private fun validateJsonWithSchema(jsonObject: JSONObject) {
    Validator.builder()
      .failEarly()
      .build()
      .performValidation(defaultsSchema, jsonObject)
  }

  /**
   * Builds a map of commit scopes given a keyed JSON object.
   */
  private fun buildScopes(jsonScopes: JSONObject?): Map<String, JsonCommitScope> =
    when (jsonScopes) {
      null -> emptyMap()
      else -> jsonScopes.keySet()
        .associateWith { scopeName ->
          val jsonScope = jsonScopes.getJSONObject(scopeName)
          JsonCommitScope(jsonScope.optString("description"))
        }
    }

  /**
   * Loads the JSON Schema used to validate the default commit types and scopes JSON file.
   */
  private fun loadDefaultsSchema(): Schema {
    val schemaInputStream = getResourceAsStream("/defaults/${CCConstants.DEFAULT_SCHEMA}")
    val schemaReader = BufferedReader(InputStreamReader(schemaInputStream, StandardCharsets.UTF_8))
    val schemaJson = JSONObject(JSONTokener(schemaReader))
    return SchemaLoader.load(schemaJson)
  }

  internal class JsonCommitType(
    var description: String? = null,
    var scopes: Map<String, JsonCommitScope>? = null
  )

  internal class JsonCommitScope(
    var description: String? = null
  )
}
