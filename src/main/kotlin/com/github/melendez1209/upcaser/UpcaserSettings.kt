package com.github.melendez1209.upcaser

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.github.melendez1209.upcaser.core.CapitalizationRules

/**
 * Settings for the Upcaser plugin
 */
@Service(Service.Level.APP)
@State(name = "UpcaserSettings", storages = [Storage("upcaser.xml")])
class UpcaserSettings : PersistentStateComponent<UpcaserSettings.State> {

    data class State(
        var enabled: Boolean = true,
        var enabledForPeriod: Boolean = true,
        var enabledForExclamation: Boolean = true,
        var enabledForQuestion: Boolean = true,
        var enabledForEllipsis: Boolean = true,
        var enabledForMarkdownHeader: Boolean = true,
        var enabledForMarkdownList: Boolean = true,
        var enabledInTextBoxes: Boolean = true,
        var autoAddSpace: Boolean = true,
        var showInStatusBar: Boolean = true,
        var toggleShortcut: String = ShortcutManager.DEFAULT_TOGGLE_SHORTCUT,
        var upcaseSelectionShortcut: String = ShortcutManager.DEFAULT_UPCASE_SHORTCUT
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): UpcaserSettings {
            return ApplicationManager.getApplication().getService(UpcaserSettings::class.java)
        }
    }

    // Getters for easy access
    val isEnabled: Boolean get() = state.enabled
    val isPeriodEnabled: Boolean get() = state.enabledForPeriod
    val isExclamationEnabled: Boolean get() = state.enabledForExclamation
    val isQuestionEnabled: Boolean get() = state.enabledForQuestion
    val isEllipsisEnabled: Boolean get() = state.enabledForEllipsis
    val isMarkdownHeaderEnabled: Boolean get() = state.enabledForMarkdownHeader
    val isMarkdownListEnabled: Boolean get() = state.enabledForMarkdownList
    val isTextBoxesEnabled: Boolean get() = state.enabledInTextBoxes
    val isAutoAddSpaceEnabled: Boolean get() = state.autoAddSpace
    val isShowInStatusBarEnabled: Boolean get() = state.showInStatusBar
    val toggleShortcut: String get() = state.toggleShortcut
    val upcaseSelectionShortcut: String get() = state.upcaseSelectionShortcut

    // Setters for configuration
    fun setEnabled(enabled: Boolean) {
        state.enabled = enabled
    }

    fun setPeriodEnabled(enabled: Boolean) {
        state.enabledForPeriod = enabled
    }

    fun setExclamationEnabled(enabled: Boolean) {
        state.enabledForExclamation = enabled
    }

    fun setQuestionEnabled(enabled: Boolean) {
        state.enabledForQuestion = enabled
    }

    fun setEllipsisEnabled(enabled: Boolean) {
        state.enabledForEllipsis = enabled
    }

    fun setMarkdownHeaderEnabled(enabled: Boolean) {
        state.enabledForMarkdownHeader = enabled
    }

    fun setMarkdownListEnabled(enabled: Boolean) {
        state.enabledForMarkdownList = enabled
    }

    fun setTextBoxesEnabled(enabled: Boolean) {
        state.enabledInTextBoxes = enabled
    }

    fun setAutoAddSpaceEnabled(enabled: Boolean) {
        state.autoAddSpace = enabled
    }

    fun setShowInStatusBar(show: Boolean) {
        state.showInStatusBar = show
    }

    fun setToggleShortcut(shortcut: String) {
        state.toggleShortcut = shortcut
    }

    fun setUpcaseSelectionShortcut(shortcut: String) {
        state.upcaseSelectionShortcut = shortcut
    }
}

/**
 * Builds an immutable snapshot of the capitalisation related settings.
 */
fun UpcaserSettings.toRules(): CapitalizationRules = CapitalizationRules(
    periodEnabled = isPeriodEnabled,
    exclamationEnabled = isExclamationEnabled,
    questionEnabled = isQuestionEnabled,
    ellipsisEnabled = isEllipsisEnabled,
    markdownHeaderEnabled = isMarkdownHeaderEnabled,
    markdownListEnabled = isMarkdownListEnabled,
    autoAddSpaceEnabled = isAutoAddSpaceEnabled
) 