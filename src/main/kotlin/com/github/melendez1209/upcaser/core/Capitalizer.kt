package com.github.melendez1209.upcaser.core

/**
 * Immutable snapshot of the settings that drive the capitalisation behaviour.
 *
 * Keeping the rules in a plain data holder allows the capitalisation logic to be reused
 * from contexts that have no access to the plugin settings service, such as unit tests
 * or plain Swing text components.
 */
data class CapitalizationRules(
    val periodEnabled: Boolean = true,
    val exclamationEnabled: Boolean = true,
    val questionEnabled: Boolean = true,
    val ellipsisEnabled: Boolean = true,
    val markdownHeaderEnabled: Boolean = true,
    val autoAddSpaceEnabled: Boolean = true
)

/**
 * Pure, side effect free implementation of the Upcaser capitalisation rules.
 *
 * The rules only look at the text that precedes the caret, which makes them equally
 * applicable to file editors, IDE text fields and Swing text areas.
 */
object Capitalizer {

    private val MARKDOWN_HEADER = Regex("^#{1,6} +$")

    /**
     * Tells whether the character typed right after [textBefore] has to be capitalised.
     *
     * @param textBefore the text that precedes the caret, the typed character excluded.
     */
    fun shouldCapitalize(textBefore: CharSequence, rules: CapitalizationRules): Boolean {
        if (textBefore.isEmpty()) {
            return true // Beginning of the text
        }

        val trimmed = textBefore.trimEnd()
        if (trimmed.isEmpty()) {
            return true // Beginning of a line
        }

        if (rules.ellipsisEnabled && trimmed.endsWith("...")) {
            return isFollowedByWhitespaceOnly(trimmed.length, textBefore)
        }

        val endsSentence = when (trimmed.last()) {
            '.' -> rules.periodEnabled
            '!' -> rules.exclamationEnabled
            '?' -> rules.questionEnabled
            else -> false
        }

        return endsSentence && isFollowedByWhitespaceOnly(trimmed.length, textBefore)
    }

    /**
     * Tells whether the text before the caret is a Markdown header, e.g. `## `.
     */
    fun isMarkdownHeaderContext(textBefore: CharSequence): Boolean {
        val lastLineStart = textBefore.lastIndexOf('\n') + 1
        val lastLine = textBefore.substring(lastLineStart)
        return MARKDOWN_HEADER.matches(lastLine)
    }

    /**
     * Tells whether a space has to be inserted after the character that has just been typed.
     *
     * @param textBefore the text that precedes the freshly typed character.
     */
    fun needsSpaceAfter(insertedChar: Char, textBefore: CharSequence, rules: CapitalizationRules): Boolean {
        if (!rules.autoAddSpaceEnabled) {
            return false
        }

        return when (insertedChar) {
            '.' -> if (endsWithDoubleDot(textBefore)) rules.ellipsisEnabled else rules.periodEnabled
            '!' -> rules.exclamationEnabled
            '?' -> rules.questionEnabled
            else -> false
        }
    }

    /**
     * Tells whether the character right before the inserted one is a dot, which means that the
     * user has just completed an ellipsis.
     */
    private fun endsWithDoubleDot(textBefore: CharSequence): Boolean =
        textBefore.isNotEmpty() && textBefore[textBefore.length - 1] == '.'

    /**
     * Checks that everything between the sentence end and the caret is whitespace.
     */
    private fun isFollowedByWhitespaceOnly(index: Int, textBefore: CharSequence): Boolean {
        if (index >= textBefore.length) {
            return false
        }

        for (i in index until textBefore.length) {
            if (!textBefore[i].isWhitespace()) {
                return false
            }
        }

        return true
    }
}
