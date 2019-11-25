package com.github.lppedd.cc.configuration

import com.github.lppedd.cc.CCConstants
import com.github.lppedd.cc.api.DefaultCommitTokenProvider.JsonCommitType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import java.io.FileReader
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.atomic.*
import java.util.concurrent.locks.*
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author Edoardo Luppi
 */
internal class CCDefaultTokensService(private val project: Project) : DefaultTokensFileChangeListener {
  companion object {
    private val GSON = Gson()
    private val TYPE = object : TypeToken<Map<String, JsonCommitType>>() {}.type

    // WARN: this one must stay below TYPE, or it will be null!
    val DEFAULT_TOKENS: Map<String, JsonCommitType> = refreshTokens()

    fun getInstance(project: Project): CCDefaultTokensService =
      ServiceManager.getService(project, CCDefaultTokensService::class.java)

    fun refreshTokens(filePath: String): Map<String, JsonCommitType> =
      GSON.fromJson<Map<String, JsonCommitType>>(FileReader(filePath), TYPE) ?: emptyMap()

    private fun refreshTokens(): Map<String, JsonCommitType> {
      val path = "/defaults/${CCConstants.DEFAULT_FILE}"
      val inputStream = CCConfigService::class.java.getResourceAsStream(path)
      val reader = InputStreamReader(inputStream)
      return GSON.fromJson<Map<String, JsonCommitType>>(reader, TYPE) ?: emptyMap()
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

  fun getDefaults(): HashMap<String, JsonCommitType> {
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
