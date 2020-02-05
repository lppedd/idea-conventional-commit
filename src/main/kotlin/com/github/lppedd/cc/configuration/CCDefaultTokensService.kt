package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.api.DefaultCommitTokenProvider.JsonCommitScope
import com.github.lppedd.cc.api.DefaultCommitTokenProvider.JsonCommitType
import com.github.lppedd.cc.getResourceAsStream
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author Edoardo Luppi
 */
internal class CCDefaultTokensService(private val project: Project) : DefaultTokensFileChangeListener {
  companion object {
    /** WARN: this one must stay below TYPE, or it will be null. */
    val DEFAULT_TOKENS: Map<String, JsonCommitType> = getDefaultTokens()

    fun getInstance(project: Project): CCDefaultTokensService =
      ServiceManager.getService(project, CCDefaultTokensService::class.java)

    fun refreshTokens(filePath: String): Map<String, JsonCommitType> {
      val bufferedReader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)
      return readFile(bufferedReader)
    }

    private fun getDefaultTokens(): Map<String, JsonCommitType> {
      val path = "/defaults/${CCConstants.DEFAULT_FILE}"
      val inputStream = getResourceAsStream(path)
      val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
      return readFile(reader)
    }

    private fun readFile(reader: Reader): Map<String, JsonCommitType> {
      val jsonTokener = JSONTokener(reader)
      val jsonObject = JSONObject(jsonTokener)

      // If the inputted JSON isn't valid an exception is thrown.
      // The exception contains the validation errors which can be used to notify the user
      validateJsonWithSchema(jsonObject)

      val jsonTypes = jsonObject.getJSONObject("types")
      val jsonCommonScopes = jsonObject.optJSONObject("commonScopes")

      // Common scopes must be added to each type's scopes
      val commonScopes = buildScopes(jsonCommonScopes).toMutableMap()

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

    private fun buildScopes(jsonCommonScopes: JSONObject?): Map<String, JsonCommitScope> {
      return when (jsonCommonScopes) {
        null -> emptyMap()
        else -> jsonCommonScopes.keySet()
          .associateWith {
            val jsonScope = jsonCommonScopes.getJSONObject(it)
            JsonCommitScope(jsonScope.optString("description"))
          }
      }
    }

    private fun validateJsonWithSchema(jsonObject: JSONObject) {
      Validator.builder()
        .failEarly()
        .build()
        .performValidation(readSchema(), jsonObject)
    }

    private fun readSchema(): Schema {
      val schemaPath = "/defaults/${CCConstants.DEFAULT_SCHEMA}"
      val schemaInputStream = getResourceAsStream(schemaPath)
      val schemaReader = BufferedReader(InputStreamReader(schemaInputStream, StandardCharsets.UTF_8))
      val rawSchema = JSONObject(JSONTokener(schemaReader))
      return SchemaLoader.load(rawSchema)
    }
  }

  init {
    project.messageBus
      .connect(project)
      .subscribe(DefaultTokensFileChangeListener.TOPIC, this)
  }

  private val firstAccess = AtomicBoolean(true)
  private val config = CCConfigService.getInstance(project)
  private val rwLock = ReentrantReadWriteLock(true)
  private var defaults: Map<String, JsonCommitType> = DEFAULT_TOKENS

  override fun fileChanged(project: Project, defaults: Map<String, JsonCommitType>) {
    if (this.project == project) {
      rwLock.write {
        this.defaults = defaults
      }
    }
  }

  fun getDefaults(): Map<String, JsonCommitType> {
    val customFilePath = config.customFilePath

    if (customFilePath != null && firstAccess.compareAndSet(true, false)) {
      rwLock.write {
        defaults = try {
          refreshTokens(customFilePath)
        } catch (e: Exception) {
          emptyMap()
        }
      }
    }

    return rwLock.read {
      HashMap(defaults)
    }
  }
}
