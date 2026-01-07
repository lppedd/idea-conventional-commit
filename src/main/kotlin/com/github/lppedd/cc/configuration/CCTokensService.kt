package com.github.lppedd.cc.configuration

import com.github.erosb.jsonsKema.*
import com.github.erosb.jsonsKema.FormatValidationPolicy.ALWAYS
import com.github.lppedd.cc.CC
import com.github.lppedd.cc.getResourceAsStream
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import java.io.IOException
import java.io.Reader
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import kotlin.io.path.bufferedReader

/**
 * Manages bundled and custom commit message tokens.
 *
 * @author Edoardo Luppi
 */
@Service(Service.Level.PROJECT)
internal class CCTokensService(private val project: Project) {
  private companion object {
    private val logger = logger<CCTokensService>()
  }

  private val configService = project.service<CCConfigService>()

  /**
   * JSON Schema used to validate the default commit types and scopes JSON file.
   */
  private val tokensSchema: Schema by lazy {
    val bufferedReader = getResourceAsStream("/defaults/${CC.File.Schema}").bufferedReader()
    val schemaJson = bufferedReader.use {
      JsonParser(bufferedReader).parse()
    }

    SchemaLoader(schemaJson).load()
  }

  /**
   * Bundled commit message tokens.
   */
  private val bundledTokensModel: TokensModel by lazy {
    val jsonStr = getResourceAsStream("/defaults/${CC.File.Defaults}").bufferedReader().use(Reader::readText)
    parseJsonStr(jsonStr)
  }

  /**
   * Returns commit message tokens defined in a custom `conventionalcommit.json` file,
   * or falls back to the bundled defaults if no custom file is specified.
   *
   * @see getBundledTokens
   */
  fun getTokens(): TokensModel {
    val path = configService.customFilePath ?: findTokensFileInProjectRoot()
    return path?.let(::readTokensFromFile) ?: getBundledTokens()
  }

  /**
   * Returns commit message tokens defined in the bundled `conventionalcommit.json` file.
   */
  fun getBundledTokens(): TokensModel =
    bundledTokensModel

  /**
   * Validates a file via the inputted absolute path.
   *
   * @throws SchemaValidationException When the JSON object does not respect the schema
   */
  fun validateTokensFile(filePath: String) {
    val path = FileSystems.getDefault().getPath(filePath)

    if (Files.notExists(path)) {
      throw NoSuchFileException(filePath)
    }

    val jsonStr = path.bufferedReader().use(Reader::readText)
    tokensSchema.validateJson(jsonStr)
  }

  /**
   * Returns the user-defined co-authors.
   */
  fun getCoAuthors(): Collection<String> {
    val customCoAuthorsFilePath = configService.customCoAuthorsFilePath
    val filePath = if (customCoAuthorsFilePath == null) {
      val projectBasePath = project.basePath ?: return emptySet()
      FileSystems.getDefault().getPath(projectBasePath, CC.File.CoAuthors)
    } else {
      FileSystems.getDefault().getPath(customCoAuthorsFilePath)
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
      fileSystem.getPath(projectBasePath, CC.File.CoAuthors)
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
   * Returns the path of the tokens file in the project root directory, or `null` if there is not.
   */
  private fun findTokensFileInProjectRoot(): String? {
    val projectBasePath = project.basePath ?: return null
    return LocalFileSystem.getInstance().refreshAndFindFileByPath(projectBasePath)
      ?.findChild(CC.File.Defaults)
      ?.path
  }

  /**
   * Reads default commit types and scopes from a file in FS via its absolute path.
   *
   * @throws SchemaValidationException When the JSON object does not respect the schema
   */
  private fun readTokensFromFile(filePath: String): TokensModel {
    val path = FileSystems.getDefault().getPath(filePath)
    val jsonStr = path.bufferedReader().use(Reader::readText)
    return parseJsonStr(jsonStr)
  }

  /**
   * @throws SchemaValidationException When the JSON object does not respect the schema
   */
  private fun parseJsonStr(jsonStr: String): TokensModel {
    // If the inputted JSON isn't valid, an exception is thrown.
    // The exception contains the validation errors which can be used to notify the user
    tokensSchema.validateJson(jsonStr)

    val rootJsonObject = JSONObject(jsonStr)
    val commonScopes = buildScopes(rootJsonObject.optJSONObject("commonScopes") ?: JSONObject())
    val types = buildTypes(rootJsonObject.getJSONObject("types"), commonScopes)
    val footerTypes = when (val it = rootJsonObject.opt("footerTypes")) {
      is JSONObject -> buildFooterTypes(it)
      is JSONArray -> buildFooterTypesArray(it)
      null -> emptyMap()
      else -> error("Should never get here")
    }

    return TokensModel(types = types, footerTypes = footerTypes)
  }

  private fun buildTypes(jsonObject: JSONObject, commonScopes: List<CommitScopeModel>): Map<String, CommitTypeModel> =
    jsonObject.keySet().associateWith {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      val scopes = buildScopes(descriptor.optJSONObject("scopes") ?: JSONObject())
      CommitTypeModel(
        name = it,
        description = description,
        scopes = scopes + commonScopes,
      )
    }

  private fun buildScopes(jsonObject: JSONObject): List<CommitScopeModel> =
    jsonObject.keySet().map {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      CommitScopeModel(
        name = it,
        description = description,
      )
    }

  private fun buildFooterTypes(jsonObject: JSONObject): Map<String, CommitFooterTypeModel> =
    jsonObject.keySet().associateWith {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      val values = buildFooterValues(descriptor.optJSONObject("values") ?: JSONObject())
      CommitFooterTypeModel(
        name = it,
        description = description,
        values = values,
      )
    }

  private fun buildFooterValues(jsonObject: JSONObject): List<CommitFooterValueModel> =
    jsonObject.keySet().map {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      CommitFooterValueModel(
        name = it,
        description = description,
      )
    }

  // TODO(Edoardo): will have to remove it as point, as keeping footer values
  //  as an array does not make sense anymore
  private fun buildFooterTypesArray(jsonArray: JSONArray): Map<String, CommitFooterTypeModel> {
    val map = LinkedHashMap<String, CommitFooterTypeModel>()

    for (i in 0..<jsonArray.length()) {
      val descriptor = jsonArray.getJSONObject(i)
      val name = descriptor.optString("name")
      val description = descriptor.optString("description", "")
      map[name] = CommitFooterTypeModel(
        name = name,
        description = description,
        values = emptyList(),
      )
    }

    return map
  }

  class TokensModel(
    val types: Map<String, CommitTypeModel>,
    val footerTypes: Map<String, CommitFooterTypeModel>,
  )

  class CommitTypeModel(
    val name: String,
    val description: String,
    val scopes: List<CommitScopeModel>,
  )

  class CommitScopeModel(
    val name: String,
    val description: String,
  )

  class CommitFooterTypeModel(
    val name: String,
    val description: String,
    val values: List<CommitFooterValueModel>,
  )

  class CommitFooterValueModel(
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
