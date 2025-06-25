package com.github.melendez1209.upcaser

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

/**
 * Status bar widget to display and control Upcaser plugin state
 */
class UpcaserStatusBarWidget(project: Project) : EditorBasedWidget(project), StatusBarWidget.TextPresentation {

    companion object {
        const val ID = "UpcaserStatus"
    }

    override fun ID(): String = ID

    override fun install(statusBar: StatusBar) {
        super.install(statusBar)
        // Listen for settings changes to update the widget
        val connection = ApplicationManager.getApplication().messageBus.connect(this)
        Disposer.register(this) { connection.disconnect() }
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String {
        val settings = UpcaserSettings.getInstance()
        return if (settings.isEnabled) {
            MyBundle.message("statusbar.text.enabled")
        } else {
            MyBundle.message("statusbar.text.disabled")
        }
    }

    override fun getTooltipText(): String {
        val settings = UpcaserSettings.getInstance()
        return if (settings.isEnabled) {
            MyBundle.message("statusbar.tooltip.enabled")
        } else {
            MyBundle.message("statusbar.tooltip.disabled")
        }
    }

    override fun getClickConsumer(): Consumer<MouseEvent>? {
        return Consumer { _: MouseEvent ->
            // Toggle the plugin state
            val settings = UpcaserSettings.getInstance()
            val newState = !settings.isEnabled
            settings.setEnabled(newState)

            // Update the status bar display
            myStatusBar?.updateWidget(ID())
        }
    }

    override fun getAlignment(): Float = 0f
} 