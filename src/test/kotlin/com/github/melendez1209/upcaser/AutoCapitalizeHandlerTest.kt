package com.github.melendez1209.upcaser

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.fileTypes.PlainTextFileType

class AutoCapitalizeHandlerTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        // Ensure settings are enabled for tests
        val settings = UpcaserSettings.getInstance()
        settings.setEnabled(true)
        settings.setPeriodEnabled(true)
        settings.setExclamationEnabled(true)
        settings.setQuestionEnabled(true)
        settings.setEllipsisEnabled(true)
        settings.setAutoAddSpaceEnabled(true)
        settings.setToggleShortcut("ctrl shift X")
    }

    fun testAutoCapitalizeAfterPeriod() {
        // Create a text file
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "This is a sentence.")

        // Move the cursor to the end and add a space and lowercase letter
        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(" hello")

        // The 'h' should be automatically capitalised to 'H'
        assertEquals("This is a sentence. Hello", myFixture.editor.document.text)
    }

    fun testAutoCapitalizeAfterExclamation() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Great!")

        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(" this")

        assertEquals("Great! This", myFixture.editor.document.text)
    }

    fun testAutoCapitalizeAfterQuestion() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Really?")

        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(" yes")

        assertEquals("Really? Yes", myFixture.editor.document.text)
    }

    fun testNoCapitalizeInMiddleOfSentence() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "This is")

        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(" great")

        // Should not capitalise 'g' as it's not after sentence ending punctuation
        assertEquals("This is great", myFixture.editor.document.text)
    }

    fun testCapitalizeAtBeginningOfFile() {
        myFixture.type("hello")

        // The first letter should be capitalised
        assertEquals("Hello", myFixture.editor.document.text)
    }

    fun testNoCapitalizeForUppercaseLetters() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "End.")

        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(" A")

        // Should remain 'A' (already uppercase)
        assertEquals("End. A", myFixture.editor.document.text)
    }

    fun testNoCapitalizeForNonLetters() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "End.")

        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(" 123")

        // Numbers should not be affected
        assertEquals("End. 123", myFixture.editor.document.text)
    }

    fun testCapitalizeWithMultipleSpaces() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "End.")

        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type("   start")

        // Should capitalise even with multiple spaces
        assertEquals("End.   Start", myFixture.editor.document.text)
    }

    fun testWorksWithAllFileTypes() {
        // Test that the handler works with any file type now
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Hello.")
        
        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(" world")
        
        assertEquals("Hello. World", myFixture.editor.document.text)
    }

    fun testAutoCapitalizeAfterEllipsis() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Wait...")
        
        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(" here")
        
        assertEquals("Wait... Here", myFixture.editor.document.text)
    }

    fun testAutoAddSpaceAfterPeriod() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Hello")
        
        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(".")
        
        assertEquals("Hello. ", myFixture.editor.document.text)
    }

    fun testAutoAddSpaceAfterExclamation() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Great")
        
        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type("!")
        
        assertEquals("Great! ", myFixture.editor.document.text)
    }

    fun testAutoAddSpaceAfterQuestion() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Really")
        
        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type("?")
        
        assertEquals("Really? ", myFixture.editor.document.text)
    }

    fun testAutoAddSpaceAfterEllipsis() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Wait..")
        
        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(".")
        
        assertEquals("Wait... ", myFixture.editor.document.text)
    }

    fun testNoDoubleSpaceWhenSpaceExists() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Hello. ")
        
        myFixture.editor.caretModel.moveToOffset(file.textLength - 1) // Before the existing space
        myFixture.type("!")
        
        assertEquals("Hello.! ", myFixture.editor.document.text)
    }

    fun testEllipsisCapitalizationAndSpacing() {
        val file = myFixture.configureByText(PlainTextFileType.INSTANCE, "Wait..")
        
        myFixture.editor.caretModel.moveToOffset(file.textLength)
        myFixture.type(".hello")
        
        // Should add space after ellipsis and capitalize the 'h'
        assertEquals("Wait... Hello", myFixture.editor.document.text)
    }

    fun testShortcutSettings() {
        val settings = UpcaserSettings.getInstance()
        
        // Test default shortcut
        assertEquals("ctrl shift X", settings.toggleShortcut)
        
        // Test setting custom shortcut
        settings.setToggleShortcut("ctrl alt Z")
        assertEquals("ctrl alt Z", settings.toggleShortcut)
        
        // Test reset to default
        settings.setToggleShortcut("ctrl shift X")
        assertEquals("ctrl shift X", settings.toggleShortcut)
    }
} 