package com.github.melendez1209.upcaser.core

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Computable
import javax.swing.text.AbstractDocument

/**
 * A piece of text that can be inspected and modified by the capitalisation logic.
 *
 * The abstraction hides the differences between the IntelliJ Platform document model and
 * the plain Swing one, so that the very same rules can be applied to file editors,
 * IDE text fields and Swing text areas.
 */
interface CapitalizationTarget {

    /** The total length of the text. */
    val length: Int

    /** The text that precedes [offset]. */
    fun textBefore(offset: Int): CharSequence

    /** The text that follows [offset]. */
    fun textAfter(offset: Int): CharSequence

    /** The character at [offset]. */
    fun charAt(offset: Int): Char

    /** Replaces the `[start, end)` range with [text]. */
    fun replace(start: Int, end: Int, text: String)

    /** Inserts [text] at [offset]. */
    fun insert(offset: Int, text: String)
}

/**
 * A [CapitalizationTarget] backed by an IntelliJ Platform document.
 */
class PlatformDocumentTarget(private val document: Document) : CapitalizationTarget {

    override val length: Int
        get() = document.textLength

    override fun textBefore(offset: Int): CharSequence =
        document.charsSequence.subSequence(0, offset)

    override fun textAfter(offset: Int): CharSequence =
        document.charsSequence.subSequence(offset, document.textLength)

    override fun charAt(offset: Int): Char = document.charsSequence[offset]

    override fun replace(start: Int, end: Int, text: String) {
        runInWriteAction { document.replaceString(start, end, text) }
    }

    override fun insert(offset: Int, text: String) {
        runInWriteAction { document.insertString(offset, text) }
    }
}

/**
 * A [CapitalizationTarget] backed by a plain Swing document.
 *
 * The mutations go through [AbstractDocument], because the convenience methods of
 * [javax.swing.text.Document] are not part of the interface in every supported JDK.
 */
class SwingDocumentTarget(private val document: javax.swing.text.Document) : CapitalizationTarget {

    override val length: Int
        get() = document.length

    override fun textBefore(offset: Int): CharSequence = document.getText(0, offset)

    override fun textAfter(offset: Int): CharSequence = document.getText(offset, document.length - offset)

    override fun charAt(offset: Int): Char = document.getText(offset, 1)[0]

    override fun replace(start: Int, end: Int, text: String) {
        val target = document as? AbstractDocument ?: return
        target.replace(start, end - start, text, null)
    }

    override fun insert(offset: Int, text: String) {
        val target = document as? AbstractDocument ?: return
        target.insertString(offset, text, null)
    }
}

/**
 * The outcome of the capitalisation of a freshly inserted character.
 */
data class InsertionOutcome(
    /** The offset of the space that has been inserted, or `-1` when no space was added. */
    val spaceOffset: Int = -1,
    /** Whether the inserted character has been capitalised. */
    val capitalized: Boolean = false
)

/**
 * Applies the Upcaser rules to a target right after a single character has been inserted.
 */
object CapitalizationApplier {

    /**
     * Capitalises the freshly inserted character when needed and adds the automatic space
     * after sentence ending punctuation.
     *
     * @param offset the offset at which the character has been inserted.
     * @param insertedChar the character that has just been inserted.
     */
    fun applyInsertion(
        target: CapitalizationTarget,
        offset: Int,
        insertedChar: Char,
        rules: CapitalizationRules
    ): InsertionOutcome {
        var spaceOffset = -1

        if (Capitalizer.needsSpaceAfter(insertedChar, target.textBefore(offset), rules)) {
            val textAfter = target.textAfter(offset + 1)
            if (textAfter.isEmpty() || !textAfter[0].isWhitespace()) {
                target.insert(offset + 1, " ")
                spaceOffset = offset + 1
            }
        }

        if (!insertedChar.isLetter() || insertedChar.isUpperCase()) {
            return InsertionOutcome(spaceOffset)
        }

        val textBefore = target.textBefore(offset)
        val capitalize = Capitalizer.shouldCapitalize(textBefore, rules) ||
                (rules.markdownHeaderEnabled && Capitalizer.isMarkdownHeaderContext(textBefore))

        if (!capitalize) {
            return InsertionOutcome(spaceOffset)
        }

        target.replace(offset, offset + 1, insertedChar.uppercaseChar().toString())
        return InsertionOutcome(spaceOffset, capitalized = true)
    }
}

/**
 * Runs the given block in a write action, reusing the write access when it is already held.
 */
internal fun runInWriteAction(action: () -> Unit) {
    val application = ApplicationManager.getApplication()
    if (application.isWriteAccessAllowed) {
        action()
    } else {
        application.runWriteAction(Computable { action() })
    }
}
