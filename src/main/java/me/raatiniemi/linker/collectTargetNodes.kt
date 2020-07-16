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

import me.raatiniemi.linker.domain.Directory
import me.raatiniemi.linker.domain.Item
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

/**
 * We have to walk through each of the target directories to find the source directory of the symbolic links.
 * <p>
 * TODO: Handle the null/empty values better.
 */
internal fun collectTargetNodes(targets: List<String?>): List<Directory?> {
    return targets.stream()
        .map { s: String? -> Paths.get(s) }
        .flatMap { directory: Path? ->
            try {
                return@flatMap Files.walk(directory)
            } catch (e: IOException) {
                return@flatMap null
            }
        }
        .filter { o: Path? -> Objects.nonNull(o) }
        .filter { path: Path? -> Files.isSymbolicLink(path) }
        .map<Path> { link: Path? ->
            try {
                return@map Files.readSymbolicLink(link)
            } catch (e: IOException) {
                return@map null
            }
        }
        .filter { o: Path? -> Objects.nonNull(o) }
        .map { path: Path? -> Item(path) }
        .collect(Collectors.toList())
}
