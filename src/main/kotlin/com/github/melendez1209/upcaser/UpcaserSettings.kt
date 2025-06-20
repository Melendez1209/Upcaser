package com.github.melendez1209.upcaser

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

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
        var autoAddSpace: Boolean = true,
        var toggleShortcut: String = "ctrl shift X"
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
    val isAutoAddSpaceEnabled: Boolean get() = state.autoAddSpace
    val toggleShortcut: String get() = state.toggleShortcut

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

    fun setAutoAddSpaceEnabled(enabled: Boolean) {
        state.autoAddSpace = enabled
    }

    fun setToggleShortcut(shortcut: String) {
        state.toggleShortcut = shortcut
    }
} 