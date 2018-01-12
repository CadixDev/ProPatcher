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

import com.cloudbees.diff.ContextualPatch
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path

class ApplyPatchesTask extends DefaultTask {

    File root
    File target
    File patches
    boolean copyPosixPermissions

    @TaskAction
    void doTask() {
        // Make sure patches directory exists
        if (!patches.exists()) {
            patches.mkdirs()
        }

        // Patch target source set
        Files.walk(patches.toPath())
                .filter { path -> Files.isRegularFile(path) }
                .each { filePath ->
            ContextualPatch patch = ContextualPatch.create(filePath.toFile(), target)
            patch.patch(false)
        }

        // Copy permissions from root source set to patched source set
        Files.walk(root.toPath())
                .filter { path -> Files.isRegularFile(path) }
                .filter { path -> copyPosixPermissions }
                .each { originalPath ->
            final String relative = {
                if (originalPath.toString().startsWith(root.getCanonicalPath())) {
                    return originalPath.toString().replace(root.getCanonicalPath() + File.separator, '')
                } else {
                    return originalPath.toString()
                }
            }

            final Path modifiedPath = target.toPath().resolve(relative)
            if (Files.exists(modifiedPath)) {
                Files.setPosixFilePermissions(modifiedPath, Files.getPosixFilePermissions(originalPath))
            }
        }
    }

}
