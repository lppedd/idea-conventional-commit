package com.github.lppedd.cc

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
@Service(Service.Level.PROJECT)
internal class CCNotificationService(private val project: Project) {
  companion object {
    private val group = NotificationGroupManager.getInstance().getNotificationGroup("com.github.lppedd.cc.notifications")

    @JvmStatic
    fun getInstance(project: Project): CCNotificationService = project.service()
  }

  fun notifyError(message: String) {
    val notification = group.createNotification("Conventional Commit", message, NotificationType.ERROR)
    notification.notify(project)
  }
}
