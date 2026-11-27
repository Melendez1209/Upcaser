package com.github.melendez1209.upcaser.core

/**
 * Side effect free helpers for locating words around an offset.
 *
 * Kept separate from the platform code so that the undo handling can be unit tested
 * without an IDE.
 */
object UndoWordMatcher {

    /**
     * The word containing [index], or `null` when [index] does not hold a letter.
     */
    fun wordAt(text: CharSequence, index: Int): String? {
        if (index < 0 || index >= text.length || !text[index].isLetter()) {
            return null
        }

        var start = index
        while (start > 0 && text[start - 1].isLetter()) {
            start--
        }
        var end = index + 1
        while (end < text.length && text[end].isLetter()) {
            end++
        }
        return text.subSequence(start, end).toString()
    }

    /**
     * The offset of the first letter of the word that contains [offset].
     */
    fun wordStart(text: CharSequence, offset: Int): Int {
        var start = offset.coerceIn(0, text.length)
        while (start > 0 && text[start - 1].isLetter()) {
            start--
        }
        return start
    }

    /**
     * The word around [offset] built from characters collected during an undo.
     *
     * Undo reports deletions one character at a time, so the word has to be put back
     * together from the individual removed characters.
     */
    fun wordAtOffsets(chars: Map<Int, Char>, offset: Int): String? {
        val letter = chars[offset] ?: return null
        if (!letter.isLetter()) {
            return null
        }

        val builder = StringBuilder()
        var before = offset - 1
        while (chars[before]?.isLetter() == true) {
            builder.insert(0, chars[before])
            before--
        }
        builder.append(letter)
        var after = offset + 1
        while (chars[after]?.isLetter() == true) {
            builder.append(chars[after])
            after++
        }
        return builder.toString()
    }

    /**
     * The letters between [from] and [to], stopping at the first non-letter.
     */
    fun typedWord(text: CharSequence, from: Int, to: Int): String {
        val end = to.coerceAtMost(text.length)
        val builder = StringBuilder()
        for (index in from until end) {
            val character = text[index]
            if (!character.isLetter()) {
                break
            }
            builder.append(character)
        }
        return builder.toString()
    }
}
