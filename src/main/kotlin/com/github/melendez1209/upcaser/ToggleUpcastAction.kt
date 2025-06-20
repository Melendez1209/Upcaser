package com.github.melendez1209.upcaser

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

/**
 * Action to toggle the Upcaser plugin on/off
 */
class ToggleUpcastAction : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val settings = UpcaserSettings.getInstance()
        val newState = !settings.isEnabled
        settings.setEnabled(newState)

        // Show notification
        val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Upcaser")
        val message = if (newState) {
            MyBundle.message("notification.enabled")
        } else {
            MyBundle.message("notification.disabled")
        }

        notificationGroup.createNotification(
            MyBundle.message("notification.title"),
            message,
            NotificationType.INFORMATION
        ).notify(e.project)
    }

    override fun update(e: AnActionEvent) {
        val settings = UpcaserSettings.getInstance()
        e.presentation.text = if (settings.isEnabled) {
            MyBundle.message("action.disable")
        } else {
            MyBundle.message("action.enable")
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
