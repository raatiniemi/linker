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

import me.raatiniemi.linker.domain.Item
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * Store the mapped raw data from the source directory.
 *
 * We have to map the raw data before attempting to filter anything,
 * otherwise we can't handle grouped directories.
 *
 * The data is mapped as follows:
 *
 * Directory 1 -> []
 * Directory 2 -> [Directory 3, Directory 4]
 *
 * Depending on if the value is empty at the end of the walk determines
 * whether the directory is a group or single item.
 */
internal fun collectRawSourceNodes(
    source: Path,
    excludeDirectories: List<String>
): Map<Path, MutableList<Item>> {
    val rawMap: MutableMap<Path, MutableList<Item>> = HashMap()
    Files.walk(source, 2)
        .filter { path: Path -> Files.isDirectory(path) }
        .filter { path: Path ->
            // When walking with `Files.walk` the source directory is
            // listed along with the other directories.
            //
            // We have to filter away the source directory since we
            // only want to map two levels.
            try {
                return@filter !Files.isSameFile(path, source)
            } catch (e: IOException) {
                return@filter false
            }
        }
        .filter { path: Path ->
            // Check if the name of the directory is included within
            // the exclude directories.
            val filename = path.fileName
                .toString()
                .toLowerCase()
            !excludeDirectories.contains(filename)
        }
        .sorted()
        .forEach { path: Path ->
            // Since we are sorting the stream before building the raw
            // data, we can safely assume that parent directories will
            // appear before its children.
            //
            // Directory 1
            // Directory 2/Directory 3
            // Directory 2/Directory 4
            //
            // If the parent do not exists within the data, we have to
            // initialize it with an empty array.
            //
            // And, then just add the children.
            if (!rawMap.containsKey(path.parent)) {
                rawMap[path] = ArrayList()
                return@forEach
            }
            rawMap[path.parent]
                ?.add(Item(path))
        }

    return rawMap
}