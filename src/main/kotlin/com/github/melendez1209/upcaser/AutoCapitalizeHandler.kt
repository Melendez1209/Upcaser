package com.github.melendez1209.upcaser

import com.github.melendez1209.upcaser.core.CapitalizationApplier
import com.github.melendez1209.upcaser.core.PlatformDocumentTarget
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.EditorTextField

/**
 * Handler for auto-capitalising letters after sentence-ending punctuation
 */
class AutoCapitalizeHandler : TypedHandlerDelegate() {

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        // Check if plugin is enabled
        val settings = UpcaserSettings.getInstance()
        if (!settings.isEnabled) {
            return Result.CONTINUE
        }

        // The text boxes of the IDE are handled by TextBoxCapitalizer. Without this guard
        // both handlers would compete for the same text and the automatic space would be
        // inserted twice.
        if (EditorTextField.managesEditor(editor)) {
            return Result.CONTINUE
        }

        val caretOffset = editor.caretModel.offset
        if (caretOffset <= 0) {
            return Result.CONTINUE
        }

        val offset = caretOffset - 1
        val tracker = UndoCapitalizationTracker.getInstance()
        val suppress = tracker.shouldSuppressCapitalization(editor.document, offset)

        val outcome = CapitalizationApplier.applyInsertion(
            target = PlatformDocumentTarget(editor.document),
            offset = offset,
            insertedChar = c,
            rules = settings.toRules(),
            suppressCapitalization = suppress
        )

        if (outcome.capitalized) {
            tracker.recordCapitalized(editor.document, offset)
        }

        // Keep the caret behind the space that has just been inserted, otherwise the next
        // word would be typed in front of it.
        if (outcome.spaceOffset >= 0 && editor.caretModel.offset == outcome.spaceOffset) {
            editor.caretModel.moveToOffset(outcome.spaceOffset + 1)
        }

        return Result.CONTINUE
    }
}
