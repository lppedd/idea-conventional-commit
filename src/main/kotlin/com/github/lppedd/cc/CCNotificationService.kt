package com.github.lppedd.cc

import com.github.lppedd.cc.annotation.Compatibility
import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType

/**
 * @author Edoardo Luppi
 */
internal object CCNotificationService {
  private const val CC_GROUP = "com.github.lppedd.cc.notifications"

  private val notificationGroup = NotificationGroupManager.getInstance()
    .getNotificationGroup(CC_GROUP)

  fun createErrorNotification(message: String): Notification =
    notificationGroup.createNotification("Conventional Commit", message, NotificationType.ERROR)
}
