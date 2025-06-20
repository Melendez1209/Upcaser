package com.github.melendez1209.upcaser

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/**
 * Configurable for Upcaser plugin settings
 */
class UpcaserConfigurable : Configurable {

    private var panel: DialogPanel? = null
    private val settings = UpcaserSettings.getInstance()
    private var shortcutField: ShortcutTextField? = null

    override fun getDisplayName(): String {
        return MyBundle.message("Settings.Title")
    }

    override fun createComponent(): JComponent {
        panel = panel {
            row {
                checkBox(MyBundle.message("Settings.Enabled"))
                    .bindSelected(settings::isEnabled, settings::setEnabled)
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
            }

            group(MyBundle.message("Settings.AutoSpace")) {
                row {
                    checkBox(MyBundle.message("Settings.AutoAddSpace"))
                        .bindSelected(settings::isAutoAddSpaceEnabled, settings::setAutoAddSpaceEnabled)
                        .comment(MyBundle.message("Settings.AutoAddSpace.Description"))
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
                        shortcutField?.setShortcut("ctrl shift X")
                    }
                    button(MyBundle.message("Settings.Shortcut.Clear")) {
                        shortcutField?.clearShortcut()
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
        return panelModified || shortcutModified
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        panel?.apply()
        shortcutField?.getShortcut()?.let { newShortcut ->
            if (newShortcut != settings.toggleShortcut) {
                settings.setToggleShortcut(newShortcut)
                updateShortcutInKeymap(newShortcut)
            }
        }
    }

    override fun reset() {
        panel?.reset()
        shortcutField?.setShortcut(settings.toggleShortcut)
    }

    private fun updateShortcutInKeymap(shortcut: String) {
        try {
            ShortcutManager.updateShortcut(shortcut)
        } catch (e: Exception) {
            // Handle error updating keymap
            println("Failed to update shortcut in keymap: ${e.message}")
        }
    }

    override fun disposeUIResources() {
        panel = null
        shortcutField = null
    }
}
