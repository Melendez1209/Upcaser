package com.github.melendez1209.upcaser

import com.github.melendez1209.upcaser.core.CapitalizationApplier
import com.github.melendez1209.upcaser.core.CapitalizationTarget
import com.github.melendez1209.upcaser.core.PlatformDocumentTarget
import com.github.melendez1209.upcaser.core.SwingDocumentTarget
import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.Computable
import com.intellij.ui.EditorTextField
import java.awt.Component
import java.awt.KeyboardFocusManager
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JTextArea

/**
 * Applies the Upcaser rules to the multi line text boxes of the IDE, such as the commit
 * message field, the AI chat input or the description fields of the VCS integrations.
 *
 * The IntelliJ Platform only notifies [com.intellij.codeInsight.editorActions.TypedHandlerDelegate]
 * for editors that are backed by a PSI file, so text fields have to be handled on their own.
 * The service watches the focus owner and attaches a document listener to every multi line
 * input the user focuses.
 */
@Service(Service.Level.APP)
class TextBoxCapitalizer : Disposable {

    /**
     * A character that has been typed and still has to be processed.
     */
    private class PendingChange(var offset: Int, val character: Char)

    /**
     * The characters of a document that are waiting to be processed.
     */
    private class PendingChanges {
        val changes = ArrayList<PendingChange>()
        var scheduled = false
    }

    private val installed = AtomicBoolean(false)

    /** Listeners registered on the [EditorTextField] instances. */
    private val editorFieldListeners = WeakHashMap<EditorTextField, DocumentListener>()

    /** Listeners registered directly on the documents of the editors without a field. */
    private val documentListeners = WeakHashMap<Document, DocumentListener>()

    /** Listeners registered on plain Swing documents. */
    private val swingListeners =
        WeakHashMap<javax.swing.text.Document, javax.swing.event.DocumentListener>()

    /** The characters that are waiting to be processed, per document. */
    private val pendingPlatformChanges = WeakHashMap<Document, PendingChanges>()
    private val pendingSwingChanges = WeakHashMap<javax.swing.text.Document, PendingChanges>()

    private val focusOwnerListener = PropertyChangeListener { event: PropertyChangeEvent ->
        if (event.propertyName == FOCUS_OWNER_PROPERTY) {
            (event.newValue as? Component)?.let { handleFocusOwner(it) }
        }
    }

    /**
     * Starts watching the focus. Calling the method more than once has no effect.
     */
    fun install() {
        if (!installed.compareAndSet(false, true)) {
            return
        }

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addPropertyChangeListener(FOCUS_OWNER_PROPERTY, focusOwnerListener)

        LOG.info("Upcaser is now watching the IDE text boxes")
    }

    override fun dispose() {
        if (installed.compareAndSet(true, false)) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removePropertyChangeListener(FOCUS_OWNER_PROPERTY, focusOwnerListener)
        }

        editorFieldListeners.clear()
        documentListeners.clear()
        swingListeners.clear()
        pendingPlatformChanges.clear()
        pendingSwingChanges.clear()
    }

    internal fun handleFocusOwner(component: Component) {
        if (!isCapitalizationActive()) {
            return
        }

        // Plain Swing text areas, e.g. the legacy commit message field.
        if (component is JTextArea) {
            attachToTextArea(component)
            return
        }

        // Editors that are hosted by a text field, e.g. the modern commit message field.
        val editor = resolveTextBoxEditor(component) ?: return
        attachToEditor(editor)
    }

    /**
     * Resolves the editor of a text box, ignoring the editors of the file editors.
     */
    private fun resolveTextBoxEditor(component: Component): Editor? {
        val field = findAncestor(component, EditorTextField::class.java)
        if (field != null) {
            return field.getEditor()
        }

        val editor = findEditorInDataContext(component) ?: return null
        return if (EditorTextField.managesEditor(editor)) editor else null
    }

    /**
     * Looks for an ancestor of the given type.
     */
    private fun <T : Component> findAncestor(component: Component, type: Class<T>): T? {
        var current: Component? = component
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current)
            }
            current = current.parent
        }
        return null
    }

    private fun findEditorInDataContext(component: Component): Editor? {
        return try {
            val context = DataManager.getInstance().getDataContext(component)
            CommonDataKeys.EDITOR.getData(context)
        } catch (e: Exception) {
            null
        }
    }

    private fun attachToEditor(editor: Editor) {
        // The editors of the file editors are handled by the typed handler.
        if (!EditorTextField.managesEditor(editor)) {
            return
        }

        // Single line fields such as branch names or search boxes are left untouched.
        if (isSingleLine(editor)) {
            return
        }

        val document = editor.document
        if (!document.isWritable) {
            return
        }

        val field = findAncestor(editor.contentComponent, EditorTextField::class.java)
        if (field != null) {
            if (editorFieldListeners.containsKey(field)) {
                return
            }

            // The listener is registered on the field, so that it keeps working when the
            // field replaces its document. The listener must not reference the field, or the
            // entry of the weak map could never be collected.
            val listener = createPlatformListener()
            field.addDocumentListener(listener)
            editorFieldListeners[field] = listener
            LOG.info("Upcaser is now capitalising the text box ${field.javaClass.simpleName}")
            return
        }

        if (documentListeners.containsKey(document)) {
            return
        }

        val listener = createPlatformListener()
        document.addDocumentListener(listener)
        documentListeners[document] = listener
        LOG.info("Upcaser is now capitalising the text box ${editor.javaClass.simpleName}")
    }

    private fun attachToTextArea(textArea: JTextArea) {
        if (!textArea.isEditable) {
            return
        }

        val document = textArea.document
        if (swingListeners.containsKey(document)) {
            return
        }

        val listener = createSwingListener()
        document.addDocumentListener(listener)
        swingListeners[document] = listener
        LOG.info("Upcaser is now capitalising the text area ${textArea.javaClass.simpleName}")
    }

    private fun createPlatformListener(): DocumentListener =
        object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                // Only a plain insertion of a single character is treated as typing, which keeps
                // undo, redo and programmatic changes untouched.
                if (event.oldLength != 0 || event.newLength != 1) {
                    return
                }

                val fragment = event.newFragment
                if (fragment.length != 1) {
                    return
                }

                if (!isCapitalizationActive()) {
                    return
                }

                val document = event.document
                val pending = pendingPlatformChanges.getOrPut(document) { PendingChanges() }
                pending.changes.add(PendingChange(event.offset, fragment[0]))

                if (!pending.scheduled) {
                    pending.scheduled = true

                    // Neither the platform nor Swing allows a modification while the document
                    // listeners are notified, so the work is delayed until the next iteration
                    // of the event loop.
                    ApplicationManager.getApplication().invokeLater {
                        flushPlatformChanges(document, pending)
                    }
                }
            }
        }

    private fun flushPlatformChanges(document: Document, pending: PendingChanges) {
        pending.scheduled = false

        val changes = pending.changes.toList()
        pending.changes.clear()
        if (changes.isEmpty() || !isCapitalizationActive()) {
            return
        }

        try {
            val editor = EditorFactory.getInstance().getEditors(document).firstOrNull()
            val spaceOffset = WriteCommandAction.runWriteCommandAction(
                editor?.project,
                Computable {
                    applyChanges(PlatformDocumentTarget(document), changes)
                }
            )

            // Keep the caret behind the space that has just been inserted, otherwise the next
            // word would be typed in front of it.
            if (spaceOffset >= 0) {
                val caretModel = editor?.caretModel
                if (caretModel != null && caretModel.offset == spaceOffset) {
                    caretModel.moveToOffset(spaceOffset + 1)
                }
            }
        } catch (e: Exception) {
            LOG.warn("Unable to capitalise the text box", e)
        }
    }

    private fun createSwingListener(): javax.swing.event.DocumentListener =
        object : javax.swing.event.DocumentListener {
            override fun insertUpdate(event: javax.swing.event.DocumentEvent) {
                if (event.length != 1) {
                    return
                }

                if (!isCapitalizationActive()) {
                    return
                }

                val document = event.document
                val character = document.getText(event.offset, 1)[0]

                val pending = pendingSwingChanges.getOrPut(document) { PendingChanges() }
                pending.changes.add(PendingChange(event.offset, character))

                if (!pending.scheduled) {
                    pending.scheduled = true
                    ApplicationManager.getApplication().invokeLater {
                        flushSwingChanges(document, pending)
                    }
                }
            }

            override fun removeUpdate(event: javax.swing.event.DocumentEvent) = Unit

            override fun changedUpdate(event: javax.swing.event.DocumentEvent) = Unit
        }

    private fun flushSwingChanges(document: javax.swing.text.Document, pending: PendingChanges) {
        pending.scheduled = false

        val changes = pending.changes.toList()
        pending.changes.clear()
        if (changes.isEmpty() || !isCapitalizationActive()) {
            return
        }

        try {
            applyChanges(SwingDocumentTarget(document), changes)
        } catch (e: Exception) {
            LOG.warn("Unable to capitalise the text area", e)
        }
    }

    /**
     * Applies the rules to the characters that have been typed, in the order they were typed.
     *
     * @return the offset of the space that has been inserted, or `-1` when no space was added.
     */
    private fun applyChanges(target: CapitalizationTarget, changes: List<PendingChange>): Int {
        val rules = UpcaserSettings.getInstance().toRules()
        var spaceOffset = -1

        for (index in changes.indices) {
            val change = changes[index]
            val offset = change.offset

            // The text may have changed in the meantime, the character is only processed when
            // it is still the one that has been typed.
            if (offset < 0 || offset >= target.length || target.charAt(offset) != change.character) {
                continue
            }

            val outcome = CapitalizationApplier.applyInsertion(target, offset, change.character, rules)
            if (outcome.spaceOffset < 0) {
                continue
            }

            spaceOffset = outcome.spaceOffset

            // The space shifts the characters that are typed after it.
            for (later in index + 1 until changes.size) {
                if (changes[later].offset >= outcome.spaceOffset) {
                    changes[later].offset += 1
                }
            }
        }

        return spaceOffset
    }

    private fun isCapitalizationActive(): Boolean {
        val settings = UpcaserSettings.getInstance()
        return settings.isEnabled && settings.isTextBoxesEnabled
    }

    /**
     * Tells whether the editor is limited to a single line.
     *
     * The flag is only exposed by the editor implementation, there is no public accessor
     * for it, but the method has been part of the platform for many years.
     */
    private fun isSingleLine(editor: Editor): Boolean = (editor as? EditorImpl)?.isOneLineMode == true

    companion object {
        private const val FOCUS_OWNER_PROPERTY = "focusOwner"

        private val LOG = Logger.getInstance(TextBoxCapitalizer::class.java)

        fun getInstance(): TextBoxCapitalizer =
            ApplicationManager.getApplication().getService(TextBoxCapitalizer::class.java)
    }
}
