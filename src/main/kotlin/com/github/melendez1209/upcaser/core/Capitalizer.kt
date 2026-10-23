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
        return isMarkdownHeader(textBefore, lastLineStart, textBefore.length)
    }

    /**
     * Applies the capitalisation rules to the given text.
     *
     * The text is treated as a standalone text, so the first letter is capitalised as well.
     * The automatic space is not inserted, the text already has its own spacing.
     *
     * @param text the text to capitalise.
     */
    fun capitalise(text: CharSequence, rules: CapitalizationRules): String {
        val result = StringBuilder(text.length)
        var lineStart = 0

        for (index in text.indices) {
            val character = text[index]

            if (character.isLetter() && !character.isUpperCase()) {
                val before = PrefixView(result, 0, index)
                val capitalise = shouldCapitalize(before, rules) ||
                        (rules.markdownHeaderEnabled && isMarkdownHeader(result, lineStart, index))

                result.append(if (capitalise) character.uppercaseChar() else character)
            } else {
                result.append(character)
            }

            if (character == '\n') {
                lineStart = index + 1
            }
        }

        return result.toString()
    }

    /**
     * Checks that the `[from, to)` range is a Markdown header, e.g. `## `.
     */
    private fun isMarkdownHeader(text: CharSequence, from: Int, to: Int): Boolean {
        var index = from
        var hashes = 0

        while (index < to && text[index] == '#') {
            hashes++
            index++
        }

        if (hashes < 1 || hashes > 6) {
            return false
        }

        if (index >= to) {
            return false // A header needs at least one space
        }

        while (index < to && text[index] == ' ') {
            index++
        }

        return index == to
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

/**
 * A view over a part of a [StringBuilder], it avoids copying the text.
 */
private class PrefixView(
    private val builder: StringBuilder,
    private val start: Int,
    private val end: Int
) : CharSequence {

    override val length: Int
        get() = end - start

    override fun get(index: Int): Char = builder[start + index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        PrefixView(builder, start + startIndex, start + endIndex)

    override fun toString(): String = builder.substring(start, end)
}
