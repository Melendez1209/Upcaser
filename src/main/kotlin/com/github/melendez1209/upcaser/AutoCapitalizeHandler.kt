package com.github.melendez1209.upcaser

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Handler for auto-capitalising letters after sentence-ending punctuation
 */
class AutoCapitalizeHandler : TypedHandlerDelegate() {

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        // Check if plugin is enabled
        val settings = UpcaserSettings.getInstance()
        if (!settings.isEnabled) {
            return Result.CONTINUE
        }

        val document = editor.document
        val caretOffset = editor.caretModel.offset

        // Handle punctuation marks - add space after them
        if (settings.isAutoAddSpaceEnabled) {
            val shouldAddSpace = when (c) {
                '.' -> {
                    settings.isPeriodEnabled &&
                            // Check if this completes an ellipsis
                            if (settings.isEllipsisEnabled && caretOffset >= 3) {
                                val textBefore = document.text.substring(maxOf(0, caretOffset - 3), caretOffset)
                                !textBefore.endsWith("..")
                            } else {
                                settings.isPeriodEnabled
                            }
                }

                '!' -> settings.isExclamationEnabled
                '?' -> settings.isQuestionEnabled
                else -> false
            }

            // Special handling for ellipsis completion
            if (c == '.' && settings.isEllipsisEnabled && caretOffset >= 2) {
                val textBefore = document.text.substring(maxOf(0, caretOffset - 2), caretOffset)
                if (textBefore == "..") {
                    // This completes an ellipsis, add space after
                    val textAfterCaret = if (caretOffset < document.textLength) {
                        document.text.substring(caretOffset)
                    } else {
                        ""
                    }

                    if (textAfterCaret.isEmpty() || !textAfterCaret.first().isWhitespace()) {
                        document.insertString(caretOffset, " ")
                    }
                    return Result.CONTINUE
                }
            }

            if (shouldAddSpace) {
                // Check if there's already a space or end of document after the punctuation
                val textAfterCaret = if (caretOffset < document.textLength) {
                    document.text.substring(caretOffset)
                } else {
                    ""
                }

                if (textAfterCaret.isEmpty() || !textAfterCaret.first().isWhitespace()) {
                    document.insertString(caretOffset, " ")
                }
                return Result.CONTINUE
            }
        }

        // Only process lowercase letters for capitalization
        if (!c.isLetter() || c.isUpperCase()) {
            return Result.CONTINUE
        }

        // Get the text before the current character
        val textBeforeCaret = if (caretOffset > 1) {
            document.text.substring(0, caretOffset - 1)
        } else {
            ""
        }

        // Check if the character before current position was after a sentence ending
        if (shouldCapitalizeAfterPunctuation(textBeforeCaret, settings)) {
            // Replace the just-typed character with its uppercase version
            document.replaceString(caretOffset - 1, caretOffset, c.uppercaseChar().toString())
        }

        return Result.CONTINUE
    }


    /**
     * Check if we should capitalise the next character based on the text before it
     */
    private fun shouldCapitalizeAfterPunctuation(textBefore: String, settings: UpcaserSettings): Boolean {
        if (textBefore.isEmpty()) {
            return true // Beginning of file
        }

        // Look for pattern: sentence ending punctuation followed by whitespace
        val trimmed = textBefore.trimEnd()
        if (trimmed.isEmpty()) {
            return true // Beginning of line or after only whitespace
        }

        // Check for ellipsis (...)
        if (settings.isEllipsisEnabled && trimmed.endsWith("...")) {
            val afterPunctuation = textBefore.substring(trimmed.length)
            return afterPunctuation.isNotEmpty() && afterPunctuation.all { it.isWhitespace() }
        }

        val lastChar = trimmed.last()

        // Check if the last non-whitespace character is sentence-ending punctuation
        val shouldCapitalize = when (lastChar) {
            '.' -> settings.isPeriodEnabled
            '!' -> settings.isExclamationEnabled
            '?' -> settings.isQuestionEnabled
            else -> false
        }

        if (shouldCapitalize) {
            // Check if there are whitespaces between punctuation and the current position
            val afterPunctuation = textBefore.substring(trimmed.length)
            return afterPunctuation.isNotEmpty() && afterPunctuation.all { it.isWhitespace() }
        }

        return false
    }
}