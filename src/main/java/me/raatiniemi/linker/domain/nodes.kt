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

// Create symbolic link

internal fun createSymbolicLink(node: Node.Link): Boolean {
    return try {
        println("Creating symbolic link ${node.path} -> ${node.source}")

        Files.createSymbolicLink(node.path, node.source)
        true
    } catch (e: IOException) {
        println("Unable to create symbolic link: $e")
        false
    }
}

// Link

internal fun link(nodes: List<Node>, linkMaps: Set<LinkMap>): List<Node> {
    return nodes.flatMap(link(linkMaps))
}

private fun link(linkMaps: Set<LinkMap>): (Node) -> List<Node> {
    return { source ->
        val link = match(source, linkMaps)
        if (link != null) {
            link(link, source)
        } else {
            link(linkMaps, source)
        }
    }
}

private fun link(link: Node.Link, source: Node): List<Node> {
    return if (createSymbolicLink(link)) {
        emptyList()
    } else {
        listOf(source)
    }
}

private fun link(linkMaps: Set<LinkMap>, source: Node): List<Node> {
    return when (source) {
        is Node.Branch -> {
            val nodes = link(source.nodes, linkMaps)
            if (nodes.isNotEmpty()) {
                listOf(
                    source.copy(
                        nodes = nodes
                    )
                )
            } else {
                emptyList<Node>()
            }
        }
        is Node.Leaf -> listOf(source)
        is Node.Link -> listOf(source)
    }
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
