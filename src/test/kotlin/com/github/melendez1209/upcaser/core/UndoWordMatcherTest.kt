package com.github.melendez1209.upcaser.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests of the word helpers used by the undo handling.
 */
class UndoWordMatcherTest {

    @Test
    fun extractsTheWordContainingAnOffset() {
        assertEquals("Hello", UndoWordMatcher.wordAt("Say Hello.", 5))
        assertEquals("h", UndoWordMatcher.wordAt("h", 0))
    }

    @Test
    fun doesNotExtractAWordFromANonLetter() {
        assertNull(UndoWordMatcher.wordAt("Say Hello.", 3))
        assertNull(UndoWordMatcher.wordAt("Say", 99))
    }

    @Test
    fun findsTheStartOfTheWord() {
        assertEquals(0, UndoWordMatcher.wordStart("hello", 0))
        assertEquals(5, UndoWordMatcher.wordStart("End. hello", 6))
    }

    @Test
    fun rebuildsAnUndoneWordFromIndividualCharacters() {
        val removed = mapOf(5 to 'H', 6 to 'e', 7 to 'l', 8 to 'l', 9 to 'o', 10 to ' ')
        assertEquals("Hello", UndoWordMatcher.wordAtOffsets(removed, 5))
        assertNull(UndoWordMatcher.wordAtOffsets(removed, 10))
        assertNull(UndoWordMatcher.wordAtOffsets(removed, 99))
    }

    @Test
    fun readsTheTypedWordUpToTheGivenOffset() {
        assertEquals("hel", UndoWordMatcher.typedWord("hello world", 0, 3))
        assertEquals("hel", UndoWordMatcher.typedWord("hel. world", 0, 4))
        assertEquals("", UndoWordMatcher.typedWord("hello", 5, 6))
    }
}
