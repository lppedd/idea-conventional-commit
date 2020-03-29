package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.DEFAULT_FILE
import com.github.lppedd.cc.DEFAULT_SCHEMA
import com.github.lppedd.cc.configuration.CCDefaultTokensService.*
import com.github.lppedd.cc.getResourceAsStream
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import org.everit.json.schema.Schema
import org.everit.json.schema.Validator
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Paths

internal typealias CommitTypesMap = Map<String, JsonCommitType>
internal typealias CommitScopes = Collection<JsonCommitScope>
internal typealias CommitFooterTypes = Collection<JsonCommitFooterType>

private val EMPTY_JSON_OBJECT = JSONObject()
private val EMPTY_JSON_ARRAY = JSONArray()

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
  private val defaultsSchema by lazy {
    val schemaInputStream = getResourceAsStream("/defaults/${DEFAULT_SCHEMA}")
    val schemaReader = BufferedReader(InputStreamReader(schemaInputStream, UTF_8))
    val schemaJson = JSONObject(JSONTokener(schemaReader))
    SchemaLoader.load(schemaJson)
  }

  /** Built-in default commit types and scopes. */
  private val builtInDefaultTokens by lazy {
    val inputStream = getResourceAsStream("/defaults/${DEFAULT_FILE}")
    val reader = BufferedReader(InputStreamReader(inputStream, UTF_8))
    reader.use(::readFile)
  }

  fun getDefaultsFromCustomFile(filePath: String? = null): JsonDefaults {
    val path = filePath ?: findDefaultFilePathFromProjectRoot()
    return path?.let(::readDefaultsFromFile) ?: builtInDefaultTokens
  }

  /** Returns the built-in commit types and scopes. */
  fun getBuiltInDefaults(): JsonDefaults = builtInDefaultTokens

  /** Validates a file via the inputted absolute path. */
  fun validateDefaultsFile(filePath: String) {
    Files.newBufferedReader(Paths.get(filePath), UTF_8).use {
      defaultsSchema.validateJson(JSONObject(JSONTokener(it)))
    }
  }

  /**
   * Returns the full path of the default tokens file located
   * in the project root directory, or `null`.
   */
  private fun findDefaultFilePathFromProjectRoot(): String? =
    project.guessProjectDir()
      ?.findChild(DEFAULT_FILE)
      ?.path

  /** Reads default commit types and scopes from a file in FS via its absolute path. */
  private fun readDefaultsFromFile(filePath: String): JsonDefaults =
    Files.newBufferedReader(Paths.get(filePath), UTF_8).use(::readFile)

  /**
   * Reads a file using the inputted `Reader` and transforms the JSON content
   * in a map of commit types and their associated scopes.
   *
   * **Note that the `Reader` must be closed by the caller.**
   */
  private fun readFile(reader: Reader): JsonDefaults {
    val rootJsonObject = JSONObject(JSONTokener(reader))

    // If the inputted JSON isn't valid an exception is thrown.
    // The exception contains the validation errors which can be used to notify the user
    defaultsSchema.validateJson(rootJsonObject)

    val commonScopes = buildScopes(rootJsonObject.optJSONObject("commonScopes"))
    val types = buildTypes(rootJsonObject.getJSONObject("types"), commonScopes)
    val footerTypes = buildFooterTypes(rootJsonObject.optJSONArray("footerTypes"))

    return JsonDefaults(types, footerTypes)
  }

  private fun buildTypes(jsonObject: JSONObject, commonScopes: CommitScopes): CommitTypesMap =
    jsonObject.keySet().associateWith {
      val descriptor = jsonObject.getJSONObject(it)
      JsonCommitType(
        descriptor.optString("description"),
        commonScopes + buildScopes(descriptor.optJSONObject("scopes")),
      )
    }

  private fun buildScopes(jsonObject: JSONObject?): CommitScopes {
    val jsonScopes = jsonObject ?: EMPTY_JSON_OBJECT
    return jsonScopes.keySet().map {
      val descriptor = jsonScopes.getJSONObject(it)
      JsonCommitScope(it, descriptor.optString("description"))
    }
  }

  private fun buildFooterTypes(jsonArray: JSONArray?): List<JsonCommitFooterType> =
    (jsonArray ?: EMPTY_JSON_ARRAY)
      .iterator()
      .asSequence()
      .map {
        it as JSONObject
        JsonCommitFooterType(it.getString("name"), it.optString("description"))
      }.toList()

  class JsonDefaults(val types: CommitTypesMap, val footerTypes: CommitFooterTypes)
  class JsonCommitType(val description: String?, val scopes: CommitScopes?)
  class JsonCommitScope(val name: String, val description: String?)
  class JsonCommitFooterType(val name: String, val description: String?)
}

/** Validate a JSON object against this JSON Schema. */
private fun Schema.validateJson(jsonObject: JSONObject) {
  Validator.builder()
    .failEarly()
    .build()
    .performValidation(this, jsonObject)
}
