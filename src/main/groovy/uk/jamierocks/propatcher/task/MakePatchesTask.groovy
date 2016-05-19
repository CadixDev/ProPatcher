/*
 * This file is part of ProPatcher, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2016, Jamie Mansfield <https://github.com/jamierocks>
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

import com.cloudbees.diff.Diff
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import uk.jamierocks.propatcher.ProPatcherPlugin

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MakePatchesTask extends DefaultTask {

    File root
    File target
    File patches

    @TaskAction
    void doTask() {
        if (patches.isDirectory()) {
            patches.deleteDir() // If exists, delete directory
        }

        patches.mkdirs() // Make sure patches directory exists.

        process(root, target) // Make the patches
    }

    void process(File root, File target) {
        final List<Path> paths = new ArrayList<>()

        Files.walk(Paths.get(root.canonicalPath)).each { filePath ->
            if (Files.isRegularFile(filePath)) {
                if (!paths.contains(filePath)) {
                    paths.add(filePath)
                }
            }
        }

        Files.walk(Paths.get(target.canonicalPath)).each { filePath ->
            if (Files.isRegularFile(filePath)) {
                if (!paths.contains(filePath)) {
                    paths.add(filePath)
                }
            }
        }

        for (Path filePath : paths) {
            String relative = ''
            if (filePath.toString().startsWith(root.getCanonicalPath())) {
                relative = filePath.toString().replace(root.getCanonicalPath() + '/', '')
            } else if (filePath.toString().startsWith(target.getCanonicalPath())) {
                relative = filePath.toString().replace(target.getCanonicalPath() + '/', '')
            }

            String originalRelative = relative
            String modifiedRelative = relative

            File originalFile = new File(root, relative)
            File modifiedFile = new File(target, relative)

            if (!originalFile.exists()) {
                originalFile = ProPatcherPlugin.DEV_NULL
                originalRelative = originalFile.getCanonicalPath()
            }

            if (!modifiedFile.exists()) {
                modifiedFile = ProPatcherPlugin.DEV_NULL
                modifiedRelative = modifiedFile.getCanonicalPath()
            }

            Diff diff = Diff.diff(originalFile, modifiedFile, true)

            if (!diff.isEmpty()) {
                File patchFile = new File(patches, "${relative}.patch")
                patchFile.parentFile.mkdirs()
                patchFile.createNewFile()

                String unifiedDiff = diff.toUnifiedDiff(originalRelative, modifiedRelative,
                        new FileReader(originalFile), new FileReader(modifiedFile), 3)

                patchFile.newOutputStream().withStream {
                    s -> s.write(unifiedDiff.getBytes(Charset.forName("utf-8")))
                }
            }
        }
    }
}
