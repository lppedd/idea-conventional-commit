package com.github.lppedd.cc

import com.intellij.CommonBundle
import com.intellij.reference.SoftReference
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.util.*

/**
 * @author Edoardo Luppi
 */
object CCBundle {
  private const val BUNDLE = "messages.ConventionalCommitBundle"
  private var bundleReference: Reference<ResourceBundle?>? = null

  @JvmStatic
  operator fun get(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    val bundle = getBundle() ?: return ""
    return CommonBundle.message(bundle, key, *params)
  }

  @NonNls
  private fun getBundle(): ResourceBundle? {
    var bundle = SoftReference.dereference(bundleReference)

    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE)
      bundleReference = java.lang.ref.SoftReference(bundle)
    }

    return bundle
  }
}
