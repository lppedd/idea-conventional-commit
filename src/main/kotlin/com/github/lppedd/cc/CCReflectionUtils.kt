package com.github.lppedd.cc

/**
 * @author Edoardo Luppi
 */
@Suppress("unused")
object CCReflectionUtils {
  @Throws(
    NoSuchFieldException::class,
    SecurityException::class,
    IllegalArgumentException::class,
    IllegalAccessException::class
  )
  fun setFieldValue(fieldName: String, obj: Any, newValue: Any?) {
    obj.javaClass.getDeclaredField(fieldName).apply {
      isAccessible = true
      set(obj, newValue)
    }
  }

  @Throws(
    NoSuchFieldException::class,
    SecurityException::class,
    IllegalArgumentException::class,
    IllegalAccessException::class
  )
  fun getField(fieldName: String, obj: Any): Any? {
    return obj.javaClass.getDeclaredField(fieldName).run {
      isAccessible = true
      get(obj)
    }
  }
}
