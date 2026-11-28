package com.github.melendez1209.upcaser

import com.github.melendez1209.upcaser.core.UndoWordMatcher
import com.github.melendez1209.upcaser.core.runInWriteAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import java.util.Collections
import java.util.WeakHashMap

/**
 * Detects capitalisation that the user rejected with undo.
 *
 * When a word is capitalised by the plugin and the user undoes it, the word is remembered.
 * Typing that same word again postpones the capitalisation until it is clear whether the
 * word really is the rejected one; a postponed capitalisation is only consumed once, so
 * normal behaviour resumes afterwards.
 */
@Service(Service.Level.APP)
class UndoCapitalizationTracker : Disposable {

    private class State {
        /** Offsets of the characters that the plugin capitalised. */
        val marks = LinkedHashSet<Int>()

        /** Lowercase words the user rejected, in the order they were rejected. */
        val rejectedWords = LinkedHashSet<String>()

        /** A capitalisation that waits for the rest of the word to be typed. */
        var pending: Pending? = null

        /** Characters removed by the undo in progress, keyed by their offset. */
        val undoBuffer = HashMap<Int, Char>()
    }

    private class Pending(val wordStart: Int, val candidates: Set<String>)

    private val states = WeakHashMap<Document, State>()
    private val listening = Collections.newSetFromMap(WeakHashMap<Document, Boolean>())

    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            if (event.oldLength == 0) {
                return
            }

            val document = event.document
            if (!isUndoInProgress(document)) {
                return
            }

            val state = states[document] ?: return
            val start = event.offset
            val end = start + event.oldLength

            // Undo reports the removal one character at a time, so keep the removed
            // characters until the capitalised letter itself is undone.
            val fragment = event.oldFragment
            var removedLetter = false
            for (index in 0 until event.oldLength) {
                if (index < fragment.length) {
                    val character = fragment[index]
                    if (character.isLetter()) {
                        state.undoBuffer[start + index] = character
                        removedLetter = true
                    }
                }
            }
            if (!removedLetter) {
                // A non-letter separates two words; do not mix them in one reconstruction.
                state.undoBuffer.clear()
            }

            val undone = state.marks.filter { it in start until end }
            if (undone.isEmpty()) {
                return
            }

            for (offset in undone) {
                val word = UndoWordMatcher.wordAtOffsets(state.undoBuffer, offset) ?: continue
                state.rejectedWords.add(word.lowercase())
            }
            state.marks.removeAll(undone.toSet())
            state.pending = null
            state.undoBuffer.clear()
            trim(state.rejectedWords, MAX_REJECTED_WORDS)
        }
    }

    /**
     * Tells whether the capitalisation of the freshly typed character at [offset] has to be
     * postponed because the word is a prefix of one the user rejected with undo.
     *
     * Must be called for every typed character: a postponed decision is resolved here as
     * soon as the word turns out to be different from the rejected one.
     */
    fun shouldSuppressCapitalization(document: Document, offset: Int): Boolean {
        if (!UpcaserSettings.getInstance().isUndoSuppressionEnabled) {
            return false
        }
        val state = states[document] ?: return false
        advancePending(document, state, offset)

        val text = document.charsSequence
        if (offset < 0 || offset >= text.length || !text[offset].isLetter()) {
            return false
        }

        val wordStart = UndoWordMatcher.wordStart(text, offset)
        if (wordStart != offset) {
            return false // Only the first letter of a word starts a new decision.
        }

        val word = UndoWordMatcher.wordAt(text, offset)?.lowercase() ?: return false
        val candidates = state.rejectedWords.filter { it.startsWith(word) }.toSet()
        if (candidates.isEmpty()) {
            return false
        }

        state.pending = Pending(offset, candidates)
        return true
    }

    /**
     * Records that the plugin capitalised the character at [offset], so that an undo that
     * removes it can be recognised later.
     */
    fun recordCapitalized(document: Document, offset: Int) {
        if (!UpcaserSettings.getInstance().isUndoSuppressionEnabled) {
            return
        }
        val state = states.getOrPut(document) { State() }
        state.marks.add(offset)
        trim(state.marks, MAX_MARKS)
        ensureListening(document)
    }

    private fun advancePending(document: Document, state: State, offset: Int) {
        val pending = state.pending ?: return
        val typed = UndoWordMatcher.typedWord(document.charsSequence, pending.wordStart, offset + 1)
        if (typed.isEmpty()) {
            state.pending = null
            return
        }

        val lower = typed.lowercase()
        if (pending.candidates.any { it == lower }) {
            // The word the user rejected was typed again: leave it lowercase and forget it.
            state.rejectedWords.removeAll(pending.candidates.filter { it == lower }.toSet())
            state.pending = null
            return
        }
        if (pending.candidates.any { it.startsWith(lower) }) {
            return // Still ambiguous, wait for the rest of the word.
        }

        // The word is different after all, apply the postponed capitalisation.
        state.pending = null
        capitalize(document, pending.wordStart)
    }

    private fun capitalize(document: Document, offset: Int) {
        val text = document.charsSequence
        if (offset < 0 || offset >= text.length) {
            return
        }
        val character = text[offset]
        if (!character.isLetter() || character.isUpperCase()) {
            return
        }
        runInWriteAction {
            document.replaceString(offset, offset + 1, character.uppercaseChar().toString())
        }
        recordCapitalized(document, offset)
    }

    private fun ensureListening(document: Document) {
        if (listening.add(document)) {
            document.addDocumentListener(documentListener, this)
        }
    }

    private fun isUndoInProgress(document: Document): Boolean {
        val project = EditorFactory.getInstance().getEditors(document).firstOrNull()?.project
        if (project != null && !project.isDisposed && UndoManager.getInstance(project).isUndoInProgress) {
            return true
        }
        return UndoManager.getGlobalInstance().isUndoInProgress
    }

    private fun <T> trim(set: LinkedHashSet<T>, max: Int) {
        while (set.size > max) {
            set.remove(set.first())
        }
    }

    /** Forgets all tracked capitalisations, e.g. when the setting is toggled. */
    fun reset() {
        states.clear()
    }

    override fun dispose() {
        states.clear()
        listening.clear()
    }

    companion object {
        private const val MAX_MARKS = 64
        private const val MAX_REJECTED_WORDS = 16

        fun getInstance(): UndoCapitalizationTracker =
            ApplicationManager.getApplication().getService(UndoCapitalizationTracker::class.java)
    }
}
