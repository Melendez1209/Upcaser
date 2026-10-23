package com.github.melendez1209.upcaser

import com.github.melendez1209.upcaser.core.Capitalizer
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange

/**
 * Action that applies the Upcaser capitalisation rules to the selected text
 */
class UpcaseSelectionAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)

        // The action only makes sense when there is something to convert
        e.presentation.isEnabled = editor != null && editor.selectionModel.hasSelection()
        e.presentation.text = MyBundle.message("action.upcase.selection")
        e.presentation.description = MyBundle.message("action.upcase.selection.description")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) {
            return
        }

        val document = editor.document
        val start = selectionModel.selectionStart
        val end = selectionModel.selectionEnd

        val selected = document.getText(TextRange(start, end))
        val capitalised = Capitalizer.capitalise(selected, UpcaserSettings.getInstance().toRules())
        if (selected == capitalised) {
            return
        }

        WriteCommandAction.runWriteCommandAction(e.project) {
            document.replaceString(start, end, capitalised)
            // Keep the selection, so that the action can be repeated on the same text
            selectionModel.setSelection(start, end)
        }
    }
}
