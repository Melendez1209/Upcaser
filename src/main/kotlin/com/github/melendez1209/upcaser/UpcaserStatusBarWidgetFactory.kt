package com.github.melendez1209.upcaser

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

/**
 * Factory for creating UpcaserStatusBarWidget instances
 */
class UpcaserStatusBarWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = UpcaserStatusBarWidget.ID

    override fun getDisplayName(): String = MyBundle.message("statusbar.widget.displayname")

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget {
        return UpcaserStatusBarWidget(project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        // Default implementation is sufficient
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
} 