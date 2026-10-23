package com.github.melendez1209.upcaser

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager
import javax.swing.KeyStroke

/**
 * Manager for handling dynamic shortcut updates
 */
object ShortcutManager {

    const val TOGGLE_ACTION_ID = "upcaser.toggle"
    const val UPCASE_SELECTION_ACTION_ID = "upcaser.upcase.selection"

    const val DEFAULT_TOGGLE_SHORTCUT = "ctrl shift X"
    const val DEFAULT_UPCASE_SHORTCUT = "ctrl alt U"

    /**
     * Replaces the shortcut of the given action.
     *
     * @param shortcutText the shortcut in the plugin format, e.g. `ctrl alt U`. An empty text
     * unbinds the action.
     */
    fun updateShortcut(actionId: String, shortcutText: String) {
        val keymapManager = KeymapManager.getInstance()
        val activeKeymap = keymapManager.activeKeymap

        // Remove existing shortcuts for this action
        val existingShortcuts = activeKeymap.getShortcuts(actionId)
        existingShortcuts.forEach { shortcut ->
            activeKeymap.removeShortcut(actionId, shortcut)
        }

        // Add new shortcut if provided
        if (shortcutText.isNotBlank()) {
            try {
                val keyStroke = parseShortcutText(shortcutText)
                if (keyStroke != null) {
                    val keyboardShortcut = KeyboardShortcut(keyStroke, null)
                    activeKeymap.addShortcut(actionId, keyboardShortcut)
                }
            } catch (e: Exception) {
                // Handle parsing error
                println("Failed to parse shortcut: $shortcutText - ${e.message}")
            }
        }
    }

    private fun parseShortcutText(shortcutText: String): KeyStroke? {
        try {
            // Convert our format (e.g., "ctrl shift X") to KeyStroke format
            val parts = shortcutText.lowercase().split(" ")
            var modifiers = 0
            var keyCode: String? = null

            for (part in parts) {
                when (part) {
                    "ctrl" -> modifiers = modifiers or java.awt.event.InputEvent.CTRL_DOWN_MASK
                    "shift" -> modifiers = modifiers or java.awt.event.InputEvent.SHIFT_DOWN_MASK
                    "alt" -> modifiers = modifiers or java.awt.event.InputEvent.ALT_DOWN_MASK
                    "meta" -> modifiers = modifiers or java.awt.event.InputEvent.META_DOWN_MASK
                    else -> {
                        if (keyCode == null) {
                            keyCode = part.uppercase()
                        }
                    }
                }
            }

            if (keyCode != null) {
                // Create the keystroke string in the format IntelliJ expects
                val modifierString = buildString {
                    if (modifiers and java.awt.event.InputEvent.CTRL_DOWN_MASK != 0) append("ctrl ")
                    if (modifiers and java.awt.event.InputEvent.SHIFT_DOWN_MASK != 0) append("shift ")
                    if (modifiers and java.awt.event.InputEvent.ALT_DOWN_MASK != 0) append("alt ")
                    if (modifiers and java.awt.event.InputEvent.META_DOWN_MASK != 0) append("meta ")
                }

                val keyStrokeString = "$modifierString$keyCode"
                return KeyStroke.getKeyStroke(keyStrokeString.trim())
            }
        } catch (e: Exception) {
            println("Error parsing shortcut: ${e.message}")
        }
        return null
    }

    /**
     * Returns the shortcut of the given action, or an empty text when the action is unbound.
     */
    fun getCurrentShortcutText(actionId: String): String {
        val keymapManager = KeymapManager.getInstance()
        val activeKeymap = keymapManager.activeKeymap
        val shortcuts = activeKeymap.getShortcuts(actionId)

        val shortcut = shortcuts.filterIsInstance<KeyboardShortcut>().firstOrNull()
        if (shortcut != null) {
            return formatKeyStroke(shortcut.firstKeyStroke)
        }

        // The action is unbound
        return ""
    }

    private fun formatKeyStroke(keyStroke: KeyStroke): String {
        val modifiers = mutableListOf<String>()
        val modifierMask = keyStroke.modifiers

        if (modifierMask and java.awt.event.InputEvent.CTRL_DOWN_MASK != 0) modifiers.add("ctrl")
        if (modifierMask and java.awt.event.InputEvent.SHIFT_DOWN_MASK != 0) modifiers.add("shift")
        if (modifierMask and java.awt.event.InputEvent.ALT_DOWN_MASK != 0) modifiers.add("alt")
        if (modifierMask and java.awt.event.InputEvent.META_DOWN_MASK != 0) modifiers.add("meta")

        val keyText = java.awt.event.KeyEvent.getKeyText(keyStroke.keyCode).lowercase()

        return (modifiers + keyText).joinToString(" ")
    }
}
