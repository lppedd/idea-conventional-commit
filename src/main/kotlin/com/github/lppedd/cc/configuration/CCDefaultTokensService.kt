package com.github.lppedd.cc.configuration

import com.github.erosb.jsonsKema.*
import com.github.erosb.jsonsKema.FormatValidationPolicy.ALWAYS
import com.github.lppedd.cc.CC
import com.github.lppedd.cc.getResourceAsStream
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.NoSuchFileException

/**
 * Manages default commit types and scopes.
 *
 * @author Edoardo Luppi
 */
@Suppress("LightServiceMigrationCode")
internal class CCDefaultTokensService(private val project: Project) {
  private companion object {
    private val logger = logger<CCDefaultTokensService>()
  }

  private val configService = project.service<CCConfigService>()

  /**
   * JSON Schema used to validate the default commit types and scopes JSON file.
   */
  private val defaultsSchema: Schema by lazy {
    val schemaInputStream = getResourceAsStream("/defaults/${CC.Tokens.SchemaFile}")
    val schemaReader = BufferedReader(InputStreamReader(schemaInputStream, UTF_8))
    val schemaJson = JsonParser(schemaReader).parse()
    SchemaLoader(schemaJson).load()
  }

  /**
   * Built-in default commit types and scopes.
   *
   * @throws SchemaValidationException When the JSON object does not respect the schema
   */
  private val builtInDefaultTokens: JsonDefaults by lazy {
    val inputStream = getResourceAsStream("/defaults/${CC.Tokens.File}")
    val reader = BufferedReader(InputStreamReader(inputStream, UTF_8))
    val jsonStr = reader.use(Reader::readText)
    parseJsonStr(jsonStr)
  }

  fun getDefaultsFromCustomFile(filePath: String? = null): JsonDefaults {
    val path = filePath ?: findDefaultFilePathFromProjectRoot()
    return path?.let(::readDefaultsFromFile) ?: builtInDefaultTokens
  }

  /**
   * Returns the built-in commit types and scopes.
   */
  fun getBuiltInDefaults(): JsonDefaults =
    builtInDefaultTokens

  /**
   * Validates a file via the inputted absolute path.
   *
   * @throws SchemaValidationException When the JSON object does not respect the schema
   */
  fun validateDefaultsFile(filePath: String) {
    val path = FileSystems.getDefault().getPath(filePath)

    if (Files.notExists(path)) {
      throw NoSuchFileException(filePath)
    }

    val jsonStr = Files.newBufferedReader(path, UTF_8).use(Reader::readText)
    defaultsSchema.validateJson(jsonStr)
  }

  /**
   * Returns the user-defined co-authors.
   */
  fun getCoAuthors(): Collection<String> {
    val customCoAuthorsFilePath = configService.customCoAuthorsFilePath
    val fileSystem = FileSystems.getDefault()
    val filePath = if (customCoAuthorsFilePath == null) {
      val projectBasePath = project.basePath ?: return emptySet()
      fileSystem.getPath(projectBasePath, CC.CoAuthors.File)
    } else {
      fileSystem.getPath(customCoAuthorsFilePath)
    }

    try {
      if (Files.exists(filePath)) {
        return Files.readAllLines(filePath, UTF_8)
          .map(String::trim)
          .filter(String::isNotEmpty)
          .toSet()
      }
    } catch (e: IOException) {
      logger.error(e)
    }

    return emptySet()
  }

  /**
   * Persist the user-defined list of co-author.
   *
   * Note that the old list, if any, gets entirely replaced.
   */
  fun setCoAuthors(coAuthors: Collection<String>) {
    val customCoAuthorsFilePath = configService.customCoAuthorsFilePath
    val fileSystem = FileSystems.getDefault()
    val filePath = if (customCoAuthorsFilePath == null) {
      val projectBasePath = project.basePath ?: return
      fileSystem.getPath(projectBasePath, CC.CoAuthors.File)
    } else {
      fileSystem.getPath(customCoAuthorsFilePath)
    }

    try {
      Files.write(filePath, coAuthors, Charsets.UTF_8)
      LocalFileSystem.getInstance().refreshAndFindFileByIoFile(filePath.toFile())?.refresh(true, true)
    } catch (e: IOException) {
      logger.error(e)
      throw e
    }
  }

  /**
   * Returns the full path of the default tokens file located
   * in the project root directory, or `null`.
   */
  private fun findDefaultFilePathFromProjectRoot(): String? {
    val projectBasePath = project.basePath ?: return null
    return LocalFileSystem.getInstance().refreshAndFindFileByPath(projectBasePath)
      ?.findChild(CC.Tokens.File)
      ?.path
  }

  /**
   * Reads default commit types and scopes from a file in FS via its absolute path.
   *
   * @throws SchemaValidationException When the JSON object does not respect the schema
   */
  private fun readDefaultsFromFile(filePath: String): JsonDefaults {
    val path = FileSystems.getDefault().getPath(filePath)
    val jsonStr = Files.newBufferedReader(path, UTF_8).use(Reader::readText)
    return parseJsonStr(jsonStr)
  }

  /**
   * @throws SchemaValidationException When the JSON object does not respect the schema
   */
  private fun parseJsonStr(jsonStr: String): JsonDefaults {
    // If the inputted JSON isn't valid, an exception is thrown.
    // The exception contains the validation errors which can be used to notify the user
    defaultsSchema.validateJson(jsonStr)

    val rootJsonObject = JSONObject(jsonStr)
    val commonScopes = buildScopes(rootJsonObject.optJSONObject("commonScopes") ?: JSONObject())
    val types = buildTypes(rootJsonObject.getJSONObject("types"), commonScopes)
    val footerTypes = when (val it = rootJsonObject.opt("footerTypes")) {
      is JSONObject -> buildFooterTypes(it)
      is JSONArray -> buildFooterTypesArray(it)
      null -> emptyMap()
      else -> error("Should never get here")
    }

    return JsonDefaults(types = types, footerTypes = footerTypes)
  }

  private fun buildTypes(jsonObject: JSONObject, commonScopes: List<JsonCommitScope>): Map<String, JsonCommitType> =
    jsonObject.keySet().associateWith {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      val scopes = buildScopes(descriptor.optJSONObject("scopes") ?: JSONObject())
      JsonCommitType(
        name = it,
        description = description,
        scopes = scopes + commonScopes,
      )
    }

  private fun buildScopes(jsonObject: JSONObject): List<JsonCommitScope> =
    jsonObject.keySet().map {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      JsonCommitScope(
        name = it,
        description = description,
      )
    }

  private fun buildFooterTypes(jsonObject: JSONObject): Map<String, JsonCommitFooterType> =
    jsonObject.keySet().associateWith {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      val values = buildFooterValues(descriptor.optJSONObject("values") ?: JSONObject())
      JsonCommitFooterType(
        name = it,
        description = description,
        values = values,
      )
    }

  private fun buildFooterValues(jsonObject: JSONObject): List<JsonCommitFooterValue> =
    jsonObject.keySet().map {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      JsonCommitFooterValue(
        name = it,
        description = description,
      )
    }

  // TODO(Edoardo): will have to remove it as point, as keeping footer values
  //  as an array does not make sense anymore
  private fun buildFooterTypesArray(jsonArray: JSONArray): Map<String, JsonCommitFooterType> {
    val map = LinkedHashMap<String, JsonCommitFooterType>()

    for (i in 0..<jsonArray.length()) {
      val descriptor = jsonArray.getJSONObject(i)
      val name = descriptor.optString("name")
      val description = descriptor.optString("description", "")
      map[name] = JsonCommitFooterType(
        name = name,
        description = description,
        values = emptyList(),
      )
    }

    return map
  }

  class JsonDefaults(
    val types: Map<String, JsonCommitType>,
    val footerTypes: Map<String, JsonCommitFooterType>,
  )

  class JsonCommitType(
    val name: String,
    val description: String,
    val scopes: List<JsonCommitScope>,
  )

  class JsonCommitScope(
    val name: String,
    val description: String,
  )

  class JsonCommitFooterType(
    val name: String,
    val description: String,
    val values: List<JsonCommitFooterValue>,
  )

  class JsonCommitFooterValue(
    val name: String,
    val description: String,
  )

  @Suppress("UNCHECKED_CAST")
  private fun JSONObject.keySet(): Set<String> =
    toMap().keys as Set<String>

  /**
   * Validates a JSON object against this JSON Schema.
   *
   * @throws SchemaValidationException When the JSON object does not respect the schema
   */
  private fun Schema.validateJson(jsonStr: String) {
    val jsonValue = JsonParser(jsonStr).parse()
    val validator = Validator.create(this, ValidatorConfig(ALWAYS))
    val result = validator.validate(jsonValue)

    if (result != null) {
      throw SchemaValidationException(result)
    }
  }
}
