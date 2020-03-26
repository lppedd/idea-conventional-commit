package com.github.lppedd.cc

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType

/**
 * @author Edoardo Luppi
 */
internal object CCNotificationService {
  @Suppress("unused")
  private val notificationGroup = NotificationGroup(
    "com.github.lppedd.cc.notifications.schema",
    NotificationDisplayType.TOOL_WINDOW,
    true
  )

  fun createErrorNotification(message: String): Notification =
    CCNotification(message, NotificationType.ERROR)

  private class CCNotification(message: String, type: NotificationType) :
      Notification(
        "com.github.lppedd.cc.notifications.schema",
        null,
        "Conventional Commit",
        "",
        message,
        type,
        null
      )
}
