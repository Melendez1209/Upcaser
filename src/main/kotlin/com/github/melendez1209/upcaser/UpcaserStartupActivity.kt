package com.github.melendez1209.upcaser

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Starts the watcher that capitalises the text boxes of the IDE.
 *
 * The watcher is application wide, the activity only makes sure that it is installed as
 * soon as the IDE is up and running.
 */
class UpcaserStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().invokeLater(Runnable {
            TextBoxCapitalizer.getInstance().install()
        })
    }
}
