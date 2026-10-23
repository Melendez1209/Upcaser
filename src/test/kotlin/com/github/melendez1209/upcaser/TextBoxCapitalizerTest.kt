package com.github.melendez1209.upcaser

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.EditorTextField
import javax.swing.JTextArea

/**
 * Tests of the capitalisation inside the text boxes of the IDE.
 */
class TextBoxCapitalizerTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()

        val settings = UpcaserSettings.getInstance()
        settings.setEnabled(true)
        settings.setTextBoxesEnabled(true)
        settings.setPeriodEnabled(true)
        settings.setExclamationEnabled(true)
        settings.setQuestionEnabled(true)
        settings.setEllipsisEnabled(true)
        settings.setMarkdownHeaderEnabled(true)
        settings.setAutoAddSpaceEnabled(true)
    }

    fun testCapitalisesInsideSwingTextArea() {
        val textArea = JTextArea()
        TextBoxCapitalizer.getInstance().handleFocusOwner(textArea)

        type(textArea, "hello.world")

        assertEquals("Hello. World", textArea.text)
    }

    fun testDoesNotCapitaliseInsideSwingTextAreaWhenDisabled() {
        val settings = UpcaserSettings.getInstance()
        settings.setTextBoxesEnabled(false)

        val textArea = JTextArea()
        TextBoxCapitalizer.getInstance().handleFocusOwner(textArea)

        type(textArea, "hello.world")

        assertEquals("hello.world", textArea.text)
    }

    fun testCapitalisesInsideEditorTextField() {
        val editor = createEditorTextField(oneLine = false)

        type(editor, "hello.world")

        assertEquals("Hello. World", editor.document.text)
    }

    fun testCapitalisesTheFirstLetterOfAnEditorTextField() {
        val editor = createEditorTextField(oneLine = false)

        type(editor, "hello")

        assertEquals("Hello", editor.document.text)
    }

    fun testDoesNotCapitaliseInsideSingleLineTextFields() {
        val editor = createEditorTextField(oneLine = true)

        type(editor, "hello.world")

        assertEquals("hello.world", editor.document.text)
    }

    fun testDoesNotCapitaliseReplacedCharacters() {
        val editor = createEditorTextField(oneLine = false)

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(0, "Hello. W")
            // A replacement is the way an undo is reported, it must not be capitalised again
            editor.document.replaceString(7, 8, "w")
        }
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

        assertEquals("Hello. w", editor.document.text)
    }

    private fun createEditorTextField(oneLine: Boolean): Editor {
        val field = EditorTextField(project, PlainTextFileType.INSTANCE)
        field.setOneLineMode(oneLine)

        val disposable = Disposer.newDisposable("upcaser test field")
        Disposer.register(testRootDisposable, disposable)
        field.setDisposedWith(disposable)

        val editor = field.getEditor(true)!!
        TextBoxCapitalizer.getInstance().handleFocusOwner(editor.contentComponent)

        return editor
    }

    private fun type(editor: Editor, text: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            for (character in text) {
                editor.document.insertString(editor.document.textLength, character.toString())
            }
        }
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }

    private fun type(textArea: JTextArea, text: String) {
        for (character in text) {
            textArea.document.insertString(textArea.document.length, character.toString(), null)
        }
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }
}
