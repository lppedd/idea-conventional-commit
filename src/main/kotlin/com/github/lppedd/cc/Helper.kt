package com.github.lppedd.cc

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import java.awt.Window
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * @author Edoardo Luppi
 */
internal object Helper {
  @Throws(Exception::class)
  fun setFieldValue(fieldName: String, obj: Any, newValue: Any?) {
    val field = obj.javaClass.getDeclaredField(fieldName);
    field.isAccessible = true

    val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())

    field.set(obj, newValue)
  }

  fun getField(fieldName: String, obj: Any): Any? {
    val declaredField = obj.javaClass.getDeclaredField(fieldName)
    declaredField.isAccessible = true
    return declaredField.get(obj)
  }

  fun getCurrentProject(): Project? {
    val projects = ProjectManager.getInstance().openProjects
    var activeProject: Project? = null

    for (project in projects) {
      val window: Window? = WindowManager.getInstance().suggestParentWindow(project)

      if (window != null && window.isActive) {
        activeProject = project
      }
    }

    return activeProject
  }
}
