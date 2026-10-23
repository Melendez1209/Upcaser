package com.github.melendez1209.upcaser

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.Editor
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests of the action that applies the capitalisation rules to the selected text.
 */
class UpcaseSelectionActionTest : BasePlatformTestCase() {

    fun testCapitalisesTheSelectedText() {
        assertCapitalised("hello world", "Hello world")
    }

    fun testCapitalisesAfterSentenceEndingPunctuation() {
        assertCapitalised("hello. world", "Hello. World")
        assertCapitalised("hello! world", "Hello! World")
        assertCapitalised("hello? world", "Hello? World")
    }

    fun testCapitalisesAfterAnEllipsis() {
        assertCapitalised("wait... here", "Wait... Here")
    }

    fun testCapitalisesAfterAMarkdownHeader() {
        assertCapitalised("## heading", "## Heading")
    }

    fun testDoesNotCapitaliseInTheMiddleOfASentence() {
        assertCapitalised("hello world", "Hello world")
    }

    fun testCapitalisesAMultiLineSelection() {
        assertCapitalised("hello. world\nsecond line", "Hello. World\nsecond line")
    }

    fun testKeepsTheSelection() {
        myFixture.configureByText("test.txt", "hello world")
        val editor = myFixture.editor
        editor.selectionModel.setSelection(0, 5)

        performUpcaseAction()

        assertEquals(0, editor.selectionModel.selectionStart)
        assertEquals(5, editor.selectionModel.selectionEnd)
    }

    fun testIsEnabledWithSelection() {
        myFixture.configureByText("test.txt", "hello world")
        val editor = myFixture.editor
        editor.selectionModel.setSelection(0, 5)

        val action = getUpcaseAction()
        val event = createEvent(action, editor)
        action.update(event)

        assertTrue(event.presentation.isEnabled)
    }

    fun testIsDisabledWithoutSelection() {
        myFixture.configureByText("test.txt", "hello world")
        val editor = myFixture.editor
        editor.selectionModel.removeSelection()

        val action = getUpcaseAction()
        val event = createEvent(action, editor)
        action.update(event)

        assertFalse(event.presentation.isEnabled)
    }

    fun testDoesNotChangeTheTextWithoutSelection() {
        myFixture.configureByText("test.txt", "hello world")

        performUpcaseAction()

        assertEquals("hello world", myFixture.editor.document.text)
    }

    private fun assertCapitalised(text: String, expected: String) {
        myFixture.configureByText("test.txt", text)
        val editor = myFixture.editor
        editor.selectionModel.setSelection(0, text.length)

        performUpcaseAction()

        assertEquals(expected, editor.document.text)
    }

    private fun getUpcaseAction(): AnAction {
        val action = ActionManager.getInstance().getAction(ACTION_ID)
        assertNotNull("The action $ACTION_ID is not registered", action)
        return action!!
    }

    private fun performUpcaseAction() {
        val action = getUpcaseAction()
        action.actionPerformed(createEvent(action, myFixture.editor))
    }

    private fun createEvent(action: AnAction, editor: Editor): AnActionEvent {
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.EDITOR, editor)
            .add(CommonDataKeys.PROJECT, project)
            .build()

        return TestActionEvent.createTestEvent(action, dataContext)
    }

    companion object {
        private const val ACTION_ID = "upcaser.upcase.selection"
    }
}
