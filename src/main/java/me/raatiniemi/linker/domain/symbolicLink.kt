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

package me.raatiniemi.linker.domain

import java.io.IOException
import java.nio.file.Files

internal fun createSymbolicLink(node: Node.Link): Boolean {
    return try {
        println("Creating symbolic link ${node.path} -> ${node.source}")

        if (needToCreateParentDirectories(node)) {
            Files.createDirectories(node.path.parent)
        }

        Files.createSymbolicLink(node.path, node.source)
        true
    } catch (e: IOException) {
        println("Unable to create symbolic link: $e")
        false
    }
}

private fun needToCreateParentDirectories(node: Node.Link): Boolean {
    return !node.path.parent.toFile().exists()
}
