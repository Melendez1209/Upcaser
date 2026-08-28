package com.github.melendez1209.upcaser

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import com.intellij.util.ui.UIUtil
import java.awt.event.MouseEvent
import javax.swing.Timer

/**
 * Status bar widget to display and control Upcaser plugin state
 */
class UpcaserStatusBarWidget(project: Project) : EditorBasedWidget(project), StatusBarWidget.TextPresentation {

    companion object {
        const val ID = "UpcaserStatus"
    }

    private var clickTimer: Timer? = null

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
        return Consumer { event: MouseEvent ->
            if (event.clickCount >= 2) {
                // Double-click: undo the immediate single-click toggle, then open settings
                clickTimer?.stop()
                if (clickTimer != null) {
                    val settings = UpcaserSettings.getInstance()
                    settings.setEnabled(!settings.isEnabled)
                    myStatusBar?.updateWidget(ID())
                }
                clickTimer = null
                ShowSettingsUtil.getInstance().showSettingsDialog(project, UpcaserConfigurable::class.java)
            } else {
                // Single click: toggle immediately, start timer to detect double-click
                clickTimer?.stop()
                val settings = UpcaserSettings.getInstance()
                settings.setEnabled(!settings.isEnabled)
                myStatusBar?.updateWidget(ID())
                clickTimer = Timer(UIUtil.getMultiClickInterval()) {
                    clickTimer = null
                }
                clickTimer?.isRepeats = false
                clickTimer?.start()
            }
        }
    }

    override fun dispose() {
        clickTimer?.stop()
        clickTimer = null
        super.dispose()
    }

    override fun getAlignment(): Float = 0f
} 