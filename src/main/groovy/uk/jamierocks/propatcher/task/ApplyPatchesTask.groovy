/*
 * This file is part of ProPatcher, licensed under the MIT License (MIT).
 *
 * Copyright (c) Jamie Mansfield <https://www.jamierocks.uk/>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package uk.jamierocks.propatcher.task

import groovy.io.FileType

import com.cloudbees.diff.ContextualPatch
import com.cloudbees.diff.ContextualPatch.PatchStatus
import com.cloudbees.diff.PatchException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

class ApplyPatchesTask extends DefaultTask {

    @InputDirectory File target
    @InputDirectory File patches

    @TaskAction
    void doTask() {
        if (!patches.exists()) {
            patches.mkdirs() // Make sure patches directory exists
        }

        boolean failed = false
        patches.eachFileRecurse(FileType.FILES) { file ->
            if (file.path.endsWith('.patch')) {
                ContextualPatch patch = ContextualPatch.create(file, target)
                patch.patch(false).each { report ->
                    if (report.status == PatchStatus.Patched) {
                        report.originalBackupFile.delete() //lets delete the backup because spam
                    } else {
                        failed = true
                        println 'Failed to apply: ' + file
                        if (report.failure instanceof PatchException)
                            println '    ' + report.failure.message
                        else
                            report.failure.printStackTrace()
                    }
                }
            }
        }

        // Patches should always use /dev/null rather than any platform-specific locations, it should
        // be standardised across systems.
        // To the effect, we should clean up our messes - so delete any directories we make on Windows.
        def NUL = new File('/dev/null')
        if (System.getProperty('os.name').toLowerCase().contains('win') && NUL.exists())
            NUL.delete()
            
        if (failed)
            throw new RuntimeException('One or more patches failed to apply, see log for details')
    }

}
