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
import java.nio.file.Path

internal fun collectSourceNodes(source: Path, excludeDirectories: List<String>): List<Node> {
    val nodes = collectNodes(source.toFile())

    return recursiveExclusion(nodes, excludeDirectories)
}

private fun recursiveExclusion(nodes: List<Node>, excludeDirectories: List<String>): List<Node> {
    return nodes.map(recursiveExclusion(excludeDirectories))
        .filter(exclude(excludeDirectories))
}

private fun recursiveExclusion(excludeDirectories: List<String>): (Node) -> Node {
    return { node ->
        when (node) {
            is Node.Branch -> {
                node.copy(
                    nodes = recursiveExclusion(node.nodes, excludeDirectories)
                )
            }
            is Node.Leaf -> node
            is Node.Link -> node
        }
    }
}

private fun exclude(excludeDirectories: List<String>): (Node) -> Boolean {
    return { it.basename !in excludeDirectories }
}
