package com.github.melendez1209.upcaser


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

        // Update status bar widget if available
        e.project?.let { project ->
            val statusBar = com.intellij.openapi.wm.WindowManager.getInstance().getStatusBar(project)
            statusBar?.updateWidget(UpcaserStatusBarWidget.ID)
        }
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
