/*
 * linker
 * Copyright (C) 2020 raatiniemi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.raatiniemi.linker

import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Build a [Path] based from the [folder].
 *
 * @param folder Folder to use as base for [Path].
 * @param segments Segments to append to the base.
 *
 * @return Assembled path based on parameters.
 */
internal fun getPath(folder: TemporaryFolder, vararg segments: String): Path {
    return Paths.get(folder.root.absolutePath, *segments)
}

/**
 * Create a new folder structure in [folder], allow for creation of nested folders.
 *
 * @param folder Folder to use as base for new folder structure.
 * @param path Path to the last folder in structure, e.g. `first-level/second-level`.
 *
 * @return [Path] to the last folder in the structure.
 */
internal fun createNewFolder(folder: TemporaryFolder, path: String): Path {
    val segments = path.split('/')
    if (segments.size > 1) {
        var prefix = ""
        for (segment in segments.dropLast(1)) {
            prefix = "${prefix}${segment}/"
            val file = File("${folder.root.absolutePath}/$prefix")
            if (file.exists()) {
                continue
            }
            folder.newFolder(prefix)
        }
    }
    return folder.newFolder(path).toPath()
}

/**
 * Create a file structure based from [folder], allow for creation of nested parent folders.
 *
 * @param folder Folder to use as base for the new file.
 * @param path Path to the file, e.g. `first-level/second-level/node`.
 *
 * @return [Path] to the file in the new structure.
 */
internal fun createNewFile(folder: TemporaryFolder, path: String): Path {
    val segments = path.split('/')
    if (segments.size > 1) {
        val folderPath = segments.dropLast(1)
            .joinToString(separator = "/")

        createNewFolder(folder, folderPath)
    }
    return folder.newFile(path).toPath()
}
