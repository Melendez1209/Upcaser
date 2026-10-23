package com.github.melendez1209.upcaser.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests of the pure capitalisation rules.
 */
class CapitalizerTest {

    private val rules = CapitalizationRules()

    @Test
    fun capitalisesAtTheBeginningOfTheText() {
        assertTrue(Capitalizer.shouldCapitalize("", rules))
    }

    @Test
    fun capitalisesAtTheBeginningOfALine() {
        assertTrue(Capitalizer.shouldCapitalize("\n", rules))
        assertTrue(Capitalizer.shouldCapitalize("Hello.\n", rules))
    }

    @Test
    fun capitalisesAfterSentenceEndingPunctuation() {
        assertTrue(Capitalizer.shouldCapitalize("Hello. ", rules))
        assertTrue(Capitalizer.shouldCapitalize("Hello! ", rules))
        assertTrue(Capitalizer.shouldCapitalize("Hello? ", rules))
    }

    @Test
    fun handlesMultipleSpacesBetweenPunctuationAndTheWord() {
        assertTrue(Capitalizer.shouldCapitalize("Hello.   ", rules))
    }

    @Test
    fun doesNotCapitaliseWithoutWhitespace() {
        assertFalse(Capitalizer.shouldCapitalize("Hello.", rules))
        assertFalse(Capitalizer.shouldCapitalize("Hello!", rules))
        assertFalse(Capitalizer.shouldCapitalize("Hello?", rules))
    }

    @Test
    fun doesNotCapitaliseInTheMiddleOfASentence() {
        assertFalse(Capitalizer.shouldCapitalize("Hello ", rules))
        assertFalse(Capitalizer.shouldCapitalize("Hello", rules))
    }

    @Test
    fun honoursTheDisabledPunctuationMarks() {
        val withoutPeriod = rules.copy(periodEnabled = false)
        assertFalse(Capitalizer.shouldCapitalize("Hello. ", withoutPeriod))
        assertTrue(Capitalizer.shouldCapitalize("Hello! ", withoutPeriod))

        val withoutExclamation = rules.copy(exclamationEnabled = false)
        assertFalse(Capitalizer.shouldCapitalize("Hello! ", withoutExclamation))

        val withoutQuestion = rules.copy(questionEnabled = false)
        assertFalse(Capitalizer.shouldCapitalize("Hello? ", withoutQuestion))
    }

    @Test
    fun capitalisesAfterAnEllipsis() {
        assertTrue(Capitalizer.shouldCapitalize("Wait... ", rules))
        assertFalse(Capitalizer.shouldCapitalize("Wait...", rules))
    }

    @Test
    fun detectsMarkdownHeaders() {
        assertTrue(Capitalizer.isMarkdownHeaderContext("# "))
        assertTrue(Capitalizer.isMarkdownHeaderContext("## "))
        assertTrue(Capitalizer.isMarkdownHeaderContext("###### "))
        assertTrue(Capitalizer.isMarkdownHeaderContext("Text\n# "))
        assertFalse(Capitalizer.isMarkdownHeaderContext("#"))
        assertFalse(Capitalizer.isMarkdownHeaderContext("####### "))
        assertFalse(Capitalizer.isMarkdownHeaderContext("#Text"))
    }

    @Test
    fun addsSpaceAfterSentenceEndingPunctuation() {
        assertTrue(Capitalizer.needsSpaceAfter('.', "Hello", rules))
        assertTrue(Capitalizer.needsSpaceAfter('!', "Hello", rules))
        assertTrue(Capitalizer.needsSpaceAfter('?', "Hello", rules))
        assertFalse(Capitalizer.needsSpaceAfter('a', "Hello", rules))
    }

    @Test
    fun addsSpaceWhenAnEllipsisIsCompleted() {
        assertTrue(Capitalizer.needsSpaceAfter('.', "Wait..", rules))
        assertTrue(Capitalizer.needsSpaceAfter('.', "Wait.", rules))
    }

    @Test
    fun doesNotAddSpaceWhenTheFeatureIsDisabled() {
        val disabled = rules.copy(autoAddSpaceEnabled = false)
        assertFalse(Capitalizer.needsSpaceAfter('.', "Hello", disabled))
        assertFalse(Capitalizer.needsSpaceAfter('!', "Hello", disabled))
        assertFalse(Capitalizer.needsSpaceAfter('?', "Hello", disabled))
    }

    @Test
    fun doesNotAddSpaceWhenThePunctuationMarkIsDisabled() {
        val withoutPeriod = rules.copy(periodEnabled = false)
        assertFalse(Capitalizer.needsSpaceAfter('.', "Hello", withoutPeriod))

        val withoutExclamation = rules.copy(exclamationEnabled = false)
        assertFalse(Capitalizer.needsSpaceAfter('!', "Hello", withoutExclamation))

        val withoutQuestion = rules.copy(questionEnabled = false)
        assertFalse(Capitalizer.needsSpaceAfter('?', "Hello", withoutQuestion))
    }

    @Test
    fun capitaliseTextAtTheBeginning() {
        assertEquals("Hello world", Capitalizer.capitalise("hello world", rules))
    }

    @Test
    fun capitaliseTextAfterSentenceEndingPunctuation() {
        assertEquals("Hello. World", Capitalizer.capitalise("hello. world", rules))
        assertEquals("Hello! World", Capitalizer.capitalise("hello! world", rules))
        assertEquals("Hello? World", Capitalizer.capitalise("hello? world", rules))
    }

    @Test
    fun capitaliseTextAfterAnEllipsis() {
        assertEquals("Wait... Here", Capitalizer.capitalise("wait... here", rules))
    }

    @Test
    fun capitaliseTextAfterAMarkdownHeader() {
        assertEquals("## Heading", Capitalizer.capitalise("## heading", rules))
    }

    @Test
    fun capitaliseTextInTheMiddleOfASentence() {
        assertEquals("Hello world", Capitalizer.capitalise("hello world", rules))
        assertEquals("Hello world again", Capitalizer.capitalise("hello world again", rules))
    }

    @Test
    fun capitaliseTextKeepsTheExistingSpaces() {
        assertEquals("Hello. World", Capitalizer.capitalise("hello. world", rules))
        assertEquals("Hello.  World", Capitalizer.capitalise("hello.  world", rules))
    }

    @Test
    fun capitaliseTextLeavesCapitalisedTextUntouched() {
        assertEquals("Hello World", Capitalizer.capitalise("Hello World", rules))
        assertEquals("", Capitalizer.capitalise("", rules))
    }

    @Test
    fun capitaliseTextWithDisabledPunctuationMarks() {
        val withoutPeriod = rules.copy(periodEnabled = false)
        assertEquals("Hello. world", Capitalizer.capitalise("hello. world", withoutPeriod))
    }

    @Test
    fun capitaliseTextOfEverySentence() {
        val text = "first sentence. second sentence! third sentence? fourth sentence"

        assertEquals(
            "First sentence. Second sentence! Third sentence? Fourth sentence",
            Capitalizer.capitalise(text, rules)
        )
    }
}
