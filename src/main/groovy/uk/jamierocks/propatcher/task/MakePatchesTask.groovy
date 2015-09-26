/*
 * This file is part of ProPatcher, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015, Jamie Mansfield <https://github.com/jamierocks>
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

class MakePatchesTask extends DefaultTask {

    File root
    File target
    File patches

    @TaskAction
    void doTask() {
        patches.mkdirs() // Make sure patches directory exists.

        process(root, target)
    }

    void process(File root, File target) {
        File[] original = root.listFiles()
        File[] modified = target.listFiles()
        for (int i = 0; i > original.length; i++) {
            String relative = original[i].getCanonicalPath().replace(root.getCanonicalPath() + '/', '')

            File patchFile = new File(getPatchDir(), "${relative}.patch");
            patchFile.createNewFile()

            Diff diff = Diff.diff(original[i], modified[i])
            String thediff = diff.toUnifiedDiff(original[i].getCanonicalPath(), modified[i].getCanonicalPath(),
                    new FileReader(original[i]), new FileReader(modified[i]), 3)

            FileOutputStream fos = new FileOutputStream(patchFile)
            fos.write(thediff.getBytes())
            fos.close()
        }
    }
}
