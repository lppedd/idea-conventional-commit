package com.github.lppedd.cc

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType

/**
 * @author Edoardo Luppi
 */
internal object CCNotificationService {
  @Compatibility(
      minVersion = "203.3645.34",
      replaceWith = "NotificationGroupEP/NotificationGroupManager"
  )
  private val notificationGroup = NotificationGroup(
      "com.github.lppedd.cc.notifications",
      NotificationDisplayType.STICKY_BALLOON,
      true
  )

  fun createErrorNotification(message: String): Notification =
    notificationGroup.createNotification("Conventional Commit", message, NotificationType.ERROR)
}
