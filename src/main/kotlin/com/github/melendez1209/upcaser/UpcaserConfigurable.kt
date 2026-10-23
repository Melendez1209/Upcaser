package com.github.melendez1209.upcaser

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import javax.swing.JComponent

/**
 * Configurable for Upcaser plugin settings
 */
class UpcaserConfigurable : Configurable {

    private var panel: DialogPanel? = null
    private val settings = UpcaserSettings.getInstance()
    private var shortcutField: ShortcutTextField? = null
    private var upcaseShortcutField: ShortcutTextField? = null

    override fun getDisplayName(): String {
        return MyBundle.message("Settings.Title")
    }

    override fun createComponent(): JComponent {
        lateinit var enabledCheckBox: JBCheckBox
        panel = panel {
            row {
                enabledCheckBox = checkBox(MyBundle.message("Settings.Enabled"))
                    .bindSelected(settings::isEnabled, settings::setEnabled)
                    .component
            }

            group(MyBundle.message("Settings.PunctuationMarks")) {
                row {
                    checkBox(MyBundle.message("Settings.Period"))
                        .bindSelected(settings::isPeriodEnabled, settings::setPeriodEnabled)
                }
                row {
                    checkBox(MyBundle.message("Settings.Exclamation"))
                        .bindSelected(settings::isExclamationEnabled, settings::setExclamationEnabled)
                }
                row {
                    checkBox(MyBundle.message("Settings.Question"))
                        .bindSelected(settings::isQuestionEnabled, settings::setQuestionEnabled)
                }
                row {
                    checkBox(MyBundle.message("Settings.Ellipsis"))
                        .bindSelected(settings::isEllipsisEnabled, settings::setEllipsisEnabled)
                }
                row {
                    checkBox(MyBundle.message("Settings.MarkdownHeader"))
                        .bindSelected(settings::isMarkdownHeaderEnabled, settings::setMarkdownHeaderEnabled)
                }
                row {
                    checkBox(MyBundle.message("Settings.MarkdownList"))
                        .bindSelected(settings::isMarkdownListEnabled, settings::setMarkdownListEnabled)
                        .comment(MyBundle.message("Settings.MarkdownList.Description"))
                }
            }.enabledIf(enabledCheckBox.selected)

            group(MyBundle.message("Settings.AutoSpace")) {
                row {
                    checkBox(MyBundle.message("Settings.AutoAddSpace"))
                        .bindSelected(settings::isAutoAddSpaceEnabled, settings::setAutoAddSpaceEnabled)
                        .comment(MyBundle.message("Settings.AutoAddSpace.Description"))
                }
            }.enabledIf(enabledCheckBox.selected)

            group(MyBundle.message("Settings.TextBoxes")) {
                row {
                    checkBox(MyBundle.message("Settings.EnableInTextBoxes"))
                        .bindSelected(settings::isTextBoxesEnabled, settings::setTextBoxesEnabled)
                        .comment(MyBundle.message("Settings.EnableInTextBoxes.Description"))
                }
            }.enabledIf(enabledCheckBox.selected)

            group(MyBundle.message("Settings.StatusBar")) {
                row {
                    checkBox(MyBundle.message("Settings.ShowInStatusBar"))
                        .bindSelected(settings::isShowInStatusBarEnabled, settings::setShowInStatusBar)
                }
            }

            group(MyBundle.message("Settings.Shortcut")) {
                row {
                    label(MyBundle.message("Settings.Shortcut.Label"))
                    cell(ShortcutTextField().also {
                        shortcutField = it
                        it.setShortcut(settings.toggleShortcut)
                    })
                    button(MyBundle.message("Settings.Shortcut.Reset")) {
                        shortcutField?.setShortcut(DEFAULT_TOGGLE_SHORTCUT)
                    }
                    button(MyBundle.message("Settings.Shortcut.Clear")) {
                        shortcutField?.clearShortcut()
                    }
                }
                row {
                    label(MyBundle.message("Settings.Shortcut.Upcase.Label"))
                    cell(ShortcutTextField().also {
                        upcaseShortcutField = it
                        it.setShortcut(settings.upcaseSelectionShortcut)
                    })
                    button(MyBundle.message("Settings.Shortcut.Reset")) {
                        upcaseShortcutField?.setShortcut(DEFAULT_UPCASE_SHORTCUT)
                    }
                    button(MyBundle.message("Settings.Shortcut.Clear")) {
                        upcaseShortcutField?.clearShortcut()
                    }
                }.comment(MyBundle.message("Settings.Shortcut.Description"))
            }

            row {
                comment(MyBundle.message("Settings.Description"))
            }
        }
        return panel!!
    }

    override fun isModified(): Boolean {
        val panelModified = panel?.isModified() ?: false
        val shortcutModified = shortcutField?.getShortcut() != settings.toggleShortcut
        val upcaseShortcutModified =
            upcaseShortcutField?.getShortcut() != settings.upcaseSelectionShortcut
        return panelModified || shortcutModified || upcaseShortcutModified
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val showInStatusBarBefore = settings.isShowInStatusBarEnabled
        panel?.apply()
        shortcutField?.getShortcut()?.let { newShortcut ->
            if (newShortcut != settings.toggleShortcut) {
                settings.setToggleShortcut(newShortcut)
                updateShortcutInKeymap(ShortcutManager.TOGGLE_ACTION_ID, newShortcut)
            }
        }
        upcaseShortcutField?.getShortcut()?.let { newShortcut ->
            if (newShortcut != settings.upcaseSelectionShortcut) {
                settings.setUpcaseSelectionShortcut(newShortcut)
                updateShortcutInKeymap(ShortcutManager.UPCASE_SELECTION_ACTION_ID, newShortcut)
            }
        }
        if (settings.isShowInStatusBarEnabled != showInStatusBarBefore) {
            refreshStatusBarWidgets()
        }
    }

    /**
     * Makes the platform re-evaluate the status bar widgets, so that hiding or showing the
     * widget takes effect without restarting the IDE.
     *
     * [StatusBarWidgetsManager.updateAllWidgets] only refreshes widgets that are already
     * registered, so the factory has to be passed explicitly to (re)create it.
     */
    private fun refreshStatusBarWidgets() {
        for (project in ProjectManager.getInstance().openProjects) {
            project.getService(StatusBarWidgetsManager::class.java)
                ?.updateWidget(UpcaserStatusBarWidgetFactory::class.java)
        }
    }

    override fun reset() {
        panel?.reset()
        shortcutField?.setShortcut(settings.toggleShortcut)
        upcaseShortcutField?.setShortcut(settings.upcaseSelectionShortcut)
    }

    private fun updateShortcutInKeymap(actionId: String, shortcut: String) {
        try {
            ShortcutManager.updateShortcut(actionId, shortcut)
        } catch (e: Exception) {
            // Handle error updating keymap
            println("Failed to update shortcut in keymap: ${e.message}")
        }
    }

    override fun disposeUIResources() {
        panel = null
        shortcutField = null
        upcaseShortcutField = null
    }

    companion object {
        const val DEFAULT_TOGGLE_SHORTCUT = ShortcutManager.DEFAULT_TOGGLE_SHORTCUT
        const val DEFAULT_UPCASE_SHORTCUT = ShortcutManager.DEFAULT_UPCASE_SHORTCUT
    }
}
