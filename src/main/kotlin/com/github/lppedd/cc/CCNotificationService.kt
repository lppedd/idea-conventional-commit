package com.github.lppedd.cc

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType.TOOL_WINDOW
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationType.ERROR

/**
 * @author Edoardo Luppi
 */
internal object CCNotificationService {
  @Suppress("unused")
  private val notificationGroup = NotificationGroup(
    "com.github.lppedd.cc.notifications.schema",
    TOOL_WINDOW,
    true
  )

  fun createErrorNotification(message: String?): Notification =
    CCNotification(message, ERROR)

  private class CCNotification(message: String?, type: NotificationType)
    : Notification("com.github.lppedd.cc.notifications.schema", null, "Conventional Commit", "", message, type, null)
}
