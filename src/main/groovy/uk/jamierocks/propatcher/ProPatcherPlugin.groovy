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

package uk.jamierocks.propatcher

import org.gradle.api.Plugin
import org.gradle.api.Project
import uk.jamierocks.propatcher.task.ApplyPatchesTask
import uk.jamierocks.propatcher.task.MakePatchesTask
import uk.jamierocks.propatcher.task.ResetSourcesTask

class ProPatcherPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.with {
            ProPatcherExtension extension = extensions.create('patches', ProPatcherExtension)

            task('makePatches', type: MakePatchesTask)
            task('applyPatches', type: ApplyPatchesTask) {
                dependsOn 'resetSources'
            }
            task('resetSources', type: ResetSourcesTask)

            afterEvaluate {
                tasks.makePatches.with {
                    root = extension.root
                    target = extension.target
                    patches = extension.patches
                    originalPrefix = extension.originalPrefix
                    modifiedPrefix = extension.modifiedPrefix
                    threads = extension.threads
                }
                tasks.applyPatches.with {
                    target = extension.target
                    patches = extension.patches
                    threads = extension.threads
                }
                tasks.resetSources.with {
                    root = extension.root
                    target = extension.target
                    threads = extension.threads
                }
            }
        }
    }

}
