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

import me.raatiniemi.linker.domain.Node
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Collect nodes from a given [root] directory.
 *
 * @param root Path to the root directory.
 */
internal fun collectNodes(root: File): List<Node> {
    val rootPath = root.toPath()
    return root.walkTopDown()
        .maxDepth(1)
        .filter { exclude(rootPath, it.toPath()) }
        .sortedBy { it.name }
        .map { node(it) }
        .toList()
}

private fun exclude(rootPath: Path, path: Path): Boolean {
    return !Files.isSameFile(rootPath, path)
}

private fun node(file: File): Node {
    val path = file.toPath()

    return when {
        isSymbolicLink(path) -> Node.Link(path, Files.readSymbolicLink(path))
        isDirectory(file) -> Node.Branch(path, collectNodes(file))
        else -> Node.Leaf(path)
    }
}

private fun isSymbolicLink(path: Path): Boolean {
    return Files.isSymbolicLink(path)
}

private fun isDirectory(file: File): Boolean {
    return file.isDirectory
}
