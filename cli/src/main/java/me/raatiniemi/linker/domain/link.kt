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

internal fun link(nodes: List<Node>, linkMaps: Set<LinkMap>): List<Node> {
    return link(nodes, linkMaps, ::createSymbolicLink)
}

internal fun dryRunLink(nodes: List<Node>, linkMaps: Set<LinkMap>): List<Node> {
    return link(nodes, linkMaps) {
        println("Creating symbolic link ${it.path} -> ${it.source}")
        true
    }
}

private fun link(nodes: List<Node>, linkMaps: Set<LinkMap>, createLink: (Node.Link) -> Boolean): List<Node> {
    return nodes.flatMap(link(linkMaps, createLink))
}

private fun link(linkMaps: Set<LinkMap>, createLink: (Node.Link) -> Boolean): (Node) -> List<Node> {
    return { source ->
        val link = match(source, linkMaps)
        if (link != null) {
            if (createLink(link)) {
                emptyList()
            } else {
                listOf(source)
            }
        } else {
            link(linkMaps, source, createLink)
        }
    }
}

private fun link(linkMaps: Set<LinkMap>, source: Node, createLink: (Node.Link) -> Boolean): List<Node> {
    return when (source) {
        is Node.Branch -> {
            val nodes = link(source.nodes, linkMaps, createLink)
            if (nodes.isNotEmpty()) {
                listOf(
                    source.copy(
                        nodes = nodes
                    )
                )
            } else {
                emptyList()
            }
        }
        is Node.Leaf -> listOf(source)
        is Node.Link -> listOf(source)
    }
}
