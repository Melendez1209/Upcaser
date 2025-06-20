package com.github.melendez1209.upcaser

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

/**
 * Configurable for Upcaser plugin settings
 */
class UpcaserConfigurable : Configurable {

    private var panel: DialogPanel? = null
    private val settings = UpcaserSettings.getInstance()

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
            
            row {
                comment(MyBundle.message("Settings.Description"))
            }
        }
        return panel!!
    }

    override fun isModified(): Boolean {
        return panel?.isModified() ?: false
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        panel?.apply()
    }

    override fun reset() {
        panel?.reset()
    }

    override fun disposeUIResources() {
        panel = null
    }
}
