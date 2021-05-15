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
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.util.regex.Matcher
import java.util.zip.ZipFile

class ResetSourcesTask extends DefaultTask {

    @InputFile @Optional File rootZip = null
    @InputDirectory @Optional File rootDir = null
    @InputDirectory File target

    static def relative(base, file) {
        return file.path.substring(base.path.length() + 1).replaceAll(Matcher.quoteReplacement(File.separator), '/') //Replace is to normalize windows to linux/zip format
    }

    static def deleteEmpty(base) {
        def dirs = []
        base.eachFileRecurse(FileType.DIRECTORIES){ file -> if (file.list().length == 0) dirs.add(file) }
        dirs.reverse().each{ it.delete() } //Do it in reverse order do we delete deepest first
    }
    
    @TaskAction
    void doTask() {
        def existing = []
        if (!target.exists())
            target.mkdirs()
        target.eachFileRecurse(FileType.FILES){ file -> existing.add relative(target, file) }
        def root = rootZip == null ? rootDir : rootZip
        if (root == null)
            throw new InvalidUserDataException("At least one of rootZip and rootDir has to be specified!")
        if (root.isDirectory()) {
            root.eachFileRecurse { file ->
                def relative = relative(root, file)
                def output = new File(target, relative)
                if (file.isDirectory()) {
                    if (!output.exists())
                        output.mkdirs()
                } else {
                    def data = file.bytes
                    if (output.exists()) {
                        if (data != output.bytes) //We do a check of the file contents here because reading is cheaper then writing, and it saves harddrive damage, amust other things
                            output.bytes = data
                    } else {
                        if (!output.parentFile.exists())
                            output.parentFile.mkdirs()
                        output.bytes = data
                    }
                    existing.remove(relative)
                }
            }
        } else {
            def zip = new ZipFile(root)
            zip.entries().each { ent ->
                def output = new File(target, ent.name)
                if (ent.isDirectory()) {
                    if (!output.exists())
                        output.mkdirs()
                } else {
                    def data = zip.getInputStream(ent).bytes
                    if (output.exists()) {
                        if (data != output.bytes) //We do a check of the file contents here because reading is cheaper then writing, and it saves harddrive damage, amust other things
                            output.bytes = data
                    } else {
                        if (!output.parentFile.exists())
                            output.parentFile.mkdirs()
                        output.bytes = data
                    }
                    existing.remove(ent.name)
                }
            }
        }
        
        existing.each{ file -> new File(target, file).delete() } //Delete extra files
        deleteEmpty(target)
    }
}
