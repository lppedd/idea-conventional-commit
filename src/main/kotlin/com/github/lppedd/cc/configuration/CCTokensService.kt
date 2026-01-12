package com.github.lppedd.cc.configuration

import com.github.erosb.jsonsKema.*
import com.github.erosb.jsonsKema.FormatValidationPolicy.ALWAYS
import com.github.lppedd.cc.*
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject

/**
 * Manages bundled and custom commit message tokens.
 *
 * @author Edoardo Luppi
 */
@Service(Service.Level.PROJECT)
internal class CCTokensService(private val project: Project) {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): CCTokensService = project.service()
  }

  /**
   * JSON Schema used to validate the default commit types and scopes JSON file.
   */
  private val tokensSchema: Schema by lazy {
    val bufferedReader = getResourceAsStream("/defaults/${CC.File.Schema}").bufferedReader()
    val schemaJson = bufferedReader.use {
      JsonParser(bufferedReader).parse()
    }

    return@lazy SchemaLoader(schemaJson).load()
  }

  /**
   * Bundled commit message tokens.
   */
  private val bundledTokensModel: TokensModel by lazy {
    val reader = getResourceAsStream("/defaults/${CC.File.Defaults}").bufferedReader()
    val result = reader.use {
      parseTokens(it.readText())
    }

    when (result) {
      is TokensResult.Success -> return@lazy result.tokens
      is TokensResult.FileError -> error("Unexpected TokensResult.FileError. ${result.message}")
      is TokensResult.SchemaError -> error("unexpected TokensResult.SchemaError. ${result.failure}")
    }
  }

  /**
   * Returns commit message tokens defined in a custom `conventionalcommit.json` file,
   * or falls back to the bundled defaults if no custom file is specified.
   */
  fun getTokens(): TokensResult {
    val configService = CCConfigService.getInstance(project)
    val filePath = configService.customFilePath
    val file = if (filePath == null) {
      findFileUnderProjectRoot(CC.File.Defaults)
    } else {
      LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath)
    }

    return if (file != null) {
      readTokensFromFile(file)
    } else {
      TokensResult.Success(getBundledTokens())
    }
  }

  /**
   * Returns commit message tokens defined in the bundled `conventionalcommit.json` file.
   */
  fun getBundledTokens(): TokensModel =
    bundledTokensModel

  /**
   * Validates a file via the inputted absolute path.
   */
  fun validateTokensFile(file: VirtualFile): ValidationFailure? {
    val reader = file.getReliableInputStream().bufferedReader(file.charset)
    return reader.use {
      val content = it.readText()
      return@use tokensSchema.validateJson(content)
    }
  }

  /**
   * Returns the user-defined co-authors.
   */
  fun getCoAuthors(): CoAuthorsResult {
    val configService = CCConfigService.getInstance(project)
    val filePath = configService.customCoAuthorsFilePath
    val file = if (filePath == null) {
      findFileUnderProjectRoot(CC.File.CoAuthors) ?: return CoAuthorsResult.Success(emptySet())
    } else {
      LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath)
    }

    if (file == null) {
      return CoAuthorsResult.Failure(CCBundle["cc.config.coAuthors.customFile.notFound"])
    }

    if (!file.isValid || file.isDirectory) {
      return CoAuthorsResult.Failure(CCBundle["cc.config.coAuthors.customFile.notReadable"])
    }

    val reader = file.getReliableInputStream().bufferedReader(file.charset)
    val coAuthors = reader.useLines {
      it.map(String::trim)
        .filterNotEmpty()
        .toSet()
    }

    return CoAuthorsResult.Success(coAuthors)
  }

  /**
   * Persist the user-defined list of co-author.
   *
   * Note that the old list, if any, gets entirely replaced.
   */
  fun setCoAuthors(coAuthors: Set<String>): CoAuthorsResult {
    val configService = CCConfigService.getInstance(project)
    val filePath = configService.customCoAuthorsFilePath
    val file = if (filePath == null) {
      val file = findFileUnderProjectRoot(CC.File.CoAuthors)

      if (file == null) {
        // Avoid creating an empty file if the co-authors set is empty and the file doesn't exist already
        if (coAuthors.isEmpty()) {
          return CoAuthorsResult.Success(emptySet())
        }

        findFileUnderProjectRoot(CC.File.CoAuthors, createIfNotExists = true)
      } else {
        file
      }
    } else {
      LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath)
    }

    if (file == null) {
      return CoAuthorsResult.Failure(CCBundle["cc.config.coAuthors.customFile.notFound"])
    }

    if (!file.isValid || !file.isWritable || file.isDirectory) {
      return CoAuthorsResult.Failure(CCBundle["cc.config.coAuthors.customFile.notWritable"])
    }

    val content = coAuthors.joinToString(
      separator = "\n",
      postfix = "\n",
      transform = String::trim,
    )

    WriteAction.runAndWait<Throwable> {
      file.setBinaryContent(content.toByteArray(file.charset))
    }

    return CoAuthorsResult.Success(coAuthors)
  }

  /**
   * Reads default commit types and scopes from a file in FS via its absolute path.
   */
  private fun readTokensFromFile(file: VirtualFile): TokensResult {
    if (!file.isValid || file.isDirectory) {
      return TokensResult.FileError(CCBundle["cc.config.defaults.customFile.notReadable"])
    }

    val reader = file.getReliableInputStream().bufferedReader(file.charset)
    return reader.use {
      val content = it.readText()
      return@use parseTokens(content)
    }
  }

  private fun parseTokens(content: String): TokensResult {
    val failure = tokensSchema.validateJson(content)

    if (failure != null) {
      return TokensResult.SchemaError(failure)
    }

    val rootJsonObject = JSONObject(content)
    val commonScopes = buildScopes(rootJsonObject.optJSONObject("commonScopes") ?: JSONObject())
    val types = buildTypes(rootJsonObject.getJSONObject("types"), commonScopes)
    val footerTypes = when (val it = rootJsonObject.opt("footerTypes")) {
      is JSONObject -> buildFooterTypes(it)
      is JSONArray -> buildFooterTypesArray(it)
      null -> emptyList()
      else -> error("Should never get here")
    }

    return TokensResult.Success(TokensModel(types, footerTypes))
  }

  private fun findFileUnderProjectRoot(fileName: String, createIfNotExists: Boolean = false): VirtualFile? {
    val rootDir = project.findRootDir() ?: return null // Avoid throwing as otherwise buildSearchableOptions fails
    var file = rootDir.findChild(fileName)

    if (file?.isValid == true) {
      return file
    }

    // The VFS might not have discovered the file yet, so we forcefully refresh it
    file = rootDir.fileSystem.refreshAndFindFileByPath("${rootDir.path}/$fileName")

    if (file != null) {
      return file
    }

    if (createIfNotExists) {
      return WriteAction.computeAndWait<VirtualFile, Throwable> {
        rootDir.createChildData(this, fileName)
      }
    }

    return null
  }

  private fun buildTypes(jsonObject: JSONObject, commonScopes: List<CommitScopeModel>): List<CommitTypeModel> =
    jsonObject.keySet().map {
      val descriptor = jsonObject.getJSONObject(it)
      val description = descriptor.optString("description", "")
      val scopes = buildScopes(descriptor.optJSONObject("scopes") ?: JSONObject())
      CommitTypeModel(
        name = it,
        description = description,
        scopes = (scopes + commonScopes).distinctBy(CommitScopeModel::name),
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

  private fun buildFooterTypes(jsonObject: JSONObject): List<CommitFooterTypeModel> =
    jsonObject.keySet().map {
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
  private fun buildFooterTypesArray(jsonArray: JSONArray): List<CommitFooterTypeModel> {
    val list = ArrayList<CommitFooterTypeModel>()

    for (i in 0..<jsonArray.length()) {
      val descriptor = jsonArray.getJSONObject(i)
      val name = descriptor.optString("name")
      val description = descriptor.optString("description", "")
      list += CommitFooterTypeModel(
        name = name,
        description = description,
        values = emptyList(),
      )
    }

    return list.distinctBy(CommitFooterTypeModel::name)
  }

  class TokensModel(
    @JvmField val types: List<CommitTypeModel>,
    @JvmField val footerTypes: List<CommitFooterTypeModel>,
  )

  class CommitTypeModel(
    @JvmField val name: String,
    @JvmField val description: String,
    @JvmField val scopes: List<CommitScopeModel>,
  )

  class CommitScopeModel(
    @JvmField val name: String,
    @JvmField val description: String,
  )

  class CommitFooterTypeModel(
    @JvmField val name: String,
    @JvmField val description: String,
    @JvmField val values: List<CommitFooterValueModel>,
  )

  class CommitFooterValueModel(
    @JvmField val name: String,
    @JvmField val description: String,
  )

  @Suppress("UNCHECKED_CAST")
  private fun JSONObject.keySet(): Set<String> =
    toMap().keys as Set<String>

  private fun Schema.validateJson(content: String): ValidationFailure? {
    val jsonValue = JsonParser(content).parse()
    val validator = Validator.create(this, ValidatorConfig(ALWAYS))
    return validator.validate(jsonValue)
  }
}
