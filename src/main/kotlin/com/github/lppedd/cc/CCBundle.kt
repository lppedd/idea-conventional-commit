package com.github.lppedd.cc

import com.intellij.BundleBase
import com.intellij.reference.SoftReference
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.util.*
import java.lang.ref.SoftReference as JavaSoftReference

/**
 * @author Edoardo Luppi
 */
public object CCBundle {
  private const val BUNDLE = "messages.ConventionalCommitBundle"

  private var bundleReference: Reference<ResourceBundle>? = null
  private var bundleLocale: Locale? = null
  private val bundle: ResourceBundle
    get() = derefBundle()

  @JvmStatic
  public operator fun get(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
    BundleBase.message(bundle, key, *params)

  @JvmStatic
  public fun getOrDefault(@PropertyKey(resourceBundle = BUNDLE) key: String, defaultValue: String, vararg params: Any): String =
    BundleBase.messageOrDefault(bundle, key, defaultValue, *params)

  @JvmStatic
  public fun setLocale(locale: Locale) {
    bundleReference = null
    bundleLocale = locale
  }

  private fun derefBundle(): ResourceBundle {
    var ref = SoftReference.dereference(bundleReference)

    if (ref == null) {
      ref = ResourceBundle.getBundle(BUNDLE, bundleLocale ?: CCRegistry.getLocale())
      bundleReference = JavaSoftReference(ref)
    }

    return ref
  }
}
