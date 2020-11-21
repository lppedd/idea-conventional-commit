package com.github.lppedd.cc

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.ApiStatus.*
import java.lang.reflect.Constructor

/**
 * @author Edoardo Luppi
 */
@Internal
@Compatibility(
    minVersion = "192.7936",
    description = "Required to avoid a crash at startup in 192.* caused by the registryKey extension point",
    replaceWith = "registryKey extention point",
)
internal object CCRegistry {
  private val registryKeyCreator = buildRegistryKeyCreator()

  fun addKeys(vararg keys: RegistryKeyDescriptor) {
    val method = Registry::class.java.getDeclaredMethod("addKeys", List::class.java)
    method.isAccessible = true
    method.invoke(null, keys.map {
      registryKeyCreator.create(
          it.name,
          it.description,
          it.defaultValue,
          it.restartRequired,
          it.pluginId,
      )
    })
  }

  private fun buildRegistryKeyCreator(): RegistryKeyCreator {
    val clazz = Class.forName("com.intellij.openapi.util.registry.RegistryKeyDescriptor")
    return try {
      // 201.3803.32+
      val ctor = clazz.getDeclaredConstructor(
          String::class.java,
          String::class.java,
          String::class.java,
          Boolean::class.java,
          String::class.java,
      )

      ctor.isAccessible = true
      RegistryKeyCreatorNew(ctor)
    } catch (_: Exception) {
      val ctor = clazz.getDeclaredConstructor(
          String::class.java,
          String::class.java,
          String::class.java,
          Boolean::class.java,
          Boolean::class.java,
      )

      ctor.isAccessible = true
      RegistryKeyCreatorOld(ctor)
    }
  }

  internal data class RegistryKeyDescriptor(
      val name: String,
      val description: String,
      val defaultValue: String,
      val restartRequired: Boolean,
      val pluginId: String? = null,
  )

  private interface RegistryKeyCreator {
    fun create(
        name: String,
        description: String,
        defaultValue: String,
        restartRequired: Boolean,
        pluginId: String? = null,
    ): Any
  }

  private class RegistryKeyCreatorNew(private val ctor: Constructor<*>) : RegistryKeyCreator {
    override fun create(
        name: String,
        description: String,
        defaultValue: String,
        restartRequired: Boolean,
        pluginId: String?,
    ): Any = ctor.newInstance(name, description, defaultValue, restartRequired, pluginId)
  }

  private class RegistryKeyCreatorOld(private val ctor: Constructor<*>) : RegistryKeyCreator {
    override fun create(
        name: String,
        description: String,
        defaultValue: String,
        restartRequired: Boolean,
        pluginId: String?,
    ): Any = ctor.newInstance(name, description, defaultValue, restartRequired, pluginId != null)
  }
}
