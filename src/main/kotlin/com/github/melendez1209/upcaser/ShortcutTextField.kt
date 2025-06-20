package com.github.melendez1209.upcaser

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.ValidationInfo
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JTextField

/**
 * A text field for capturing and displaying keyboard shortcuts
 */
class ShortcutTextField : JTextField(), Disposable {

    private var currentShortcut: String = ""
    private var isCapturing = false

    init {
        setupTextField()
        setupValidation()
    }

    private fun setupTextField() {
        isEditable = false
        toolTipText = MyBundle.message("Settings.Shortcut.Tooltip")

        addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                startCapturing()
            }

            override fun focusLost(e: FocusEvent?) {
                stopCapturing()
            }
        })

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (isCapturing && e != null) {
                    captureShortcut(e)
                }
            }
        })
    }

    private fun setupValidation() {
        ComponentValidator(this).withValidator {
            val text = this.text
            if (text.isBlank()) {
                ValidationInfo(MyBundle.message("Settings.Shortcut.Empty"), this)
            } else if (!isValidShortcut(text)) {
                ValidationInfo(MyBundle.message("Settings.Shortcut.Invalid"), this)
            } else {
                null
            }
        }.installOn(this)
    }

    private fun startCapturing() {
        isCapturing = true
        text = MyBundle.message("Settings.Shortcut.Capturing")
        background = java.awt.Color.YELLOW.brighter()
    }

    private fun stopCapturing() {
        isCapturing = false
        background = java.awt.Color.WHITE
        if (text == MyBundle.message("Settings.Shortcut.Capturing")) {
            text = currentShortcut
        }
    }

    private fun captureShortcut(e: KeyEvent) {
        // Ignore modifier keys alone
        if (e.keyCode == KeyEvent.VK_CONTROL ||
            e.keyCode == KeyEvent.VK_SHIFT ||
            e.keyCode == KeyEvent.VK_ALT ||
            e.keyCode == KeyEvent.VK_META
        ) {
            return
        }

        val modifiers = mutableListOf<String>()
        if (e.isControlDown) modifiers.add("ctrl")
        if (e.isShiftDown) modifiers.add("shift")
        if (e.isAltDown) modifiers.add("alt")
        if (e.isMetaDown) modifiers.add("meta")

        val keyText = KeyEvent.getKeyText(e.keyCode).lowercase()
        val shortcutText = (modifiers + keyText).joinToString(" ")

        currentShortcut = shortcutText
        text = formatShortcutForDisplay(shortcutText)

        // Consume the event to prevent further processing
        e.consume()

        // Stop capturing after successful capture
        transferFocus()
    }

    private fun formatShortcutForDisplay(shortcut: String): String {
        return shortcut.split(" ").joinToString("+") { part ->
            when (part.lowercase()) {
                "ctrl" -> "Ctrl"
                "shift" -> "Shift"
                "alt" -> "Alt"
                "meta" -> "Meta"
                else -> part.uppercase()
            }
        }
    }

    private fun isValidShortcut(shortcut: String): Boolean {
        if (shortcut.isBlank()) return false
        try {
            // Try to parse the shortcut to validate it
            val parts = shortcut.lowercase().split(" ")
            return parts.isNotEmpty() && parts.any { part ->
                part !in listOf("ctrl", "shift", "alt", "meta")
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun setShortcut(shortcut: String) {
        currentShortcut = shortcut
        text = formatShortcutForDisplay(shortcut)
    }

    fun getShortcut(): String {
        return currentShortcut
    }

    fun clearShortcut() {
        currentShortcut = ""
        text = ""
    }

    override fun dispose() {
        // Clean up resources if needed
        // For this simple component, no specific cleanup is required
    }
} 