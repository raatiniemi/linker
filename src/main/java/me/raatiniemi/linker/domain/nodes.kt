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

import java.nio.file.Path
import java.nio.file.Paths

// Filter

internal fun filter(sources: List<Node>, targets: List<Node.Link>): List<Node> {
    val canonicalPathForTargets = targets.map { canonicalPath(it.source) }
    return sources.flatMap { node ->
        when (node) {
            is Node.Branch -> filter(node, canonicalPathForTargets)
            is Node.Leaf -> filter(node, canonicalPathForTargets)
            else -> listOf(node)
        }
    }
}

private fun filter(node: Node, canonicalPathForTargets: List<String>): List<Node> {
    val canonicalPath = canonicalPath(node.path)
    return if (isLinked(canonicalPath, canonicalPathForTargets)) {
        emptyList()
    } else {
        listOf(node)
    }
}

private fun canonicalPath(path: Path): String {
    return path.toFile()
        .canonicalPath
}

private fun isLinked(canonicalPath: String, canonicalPathForTargets: List<String>): Boolean {
    return canonicalPath in canonicalPathForTargets
}

// Match

internal fun match(node: Node, linkMaps: Set<LinkMap>): Node.Link? {
    return linkMaps.firstOrNull { match(node.basename, it) }
        ?.let {
            val link = Paths.get(it.target, node.basename)
            val target = Paths.get(it.prefix, node.basename)

            Node.Link(link, target)
        }
}

// Print

internal fun print(nodes: List<Node>) {
    nodes.sortedBy { it.path }
        .map(::print)
}

private fun print(node: Node) {
    when (node) {
        is Node.Branch -> {
            println(node.path)
            print(node.nodes)
        }
        is Node.Leaf -> println(node.path)
        is Node.Link -> println("${node.path} -> ${node.source}")
    }
}
