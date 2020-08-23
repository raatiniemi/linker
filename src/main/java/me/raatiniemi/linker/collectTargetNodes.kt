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
import me.raatiniemi.linker.domain.Node
import java.io.File

/**
 * Collect symbolic links from each of the targets.
 *
 * @param targets List of target directories.
 *
 * @return List of symbolic link nodes available within the targets.
 */
internal fun collectTargetNodes(targets: List<String>): List<Directory> {
    return targets.map { File(it) }
        .flatMap(::collectNodes)
        .filterIsInstance<Node.Link>()
        .toList()
}
