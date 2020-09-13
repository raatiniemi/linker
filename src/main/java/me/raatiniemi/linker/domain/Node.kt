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
import java.util.stream.Collectors

/**
 * Represents the different states for a given path.
 */
internal sealed class Node {
    abstract val path: Path

    val basename: String
        get() = path.fileName.toString()

    /**
     * Attempt to link directory if link map configuration is found.
     *
     * @param linkMaps Link map configurations.
     * @return true if item was linked, otherwise false.
     */
    abstract fun link(linkMaps: Set<LinkMap>): Boolean

    data class Branch(override val path: Path, var nodes: List<Node>) : Node() {
        /**
         * @inheritDoc
         */
        override fun link(linkMaps: Set<LinkMap>): Boolean {
            val items = nodes.stream()
                .filter { item ->
                    val value = linkMaps.stream()
                        .filter { match(item.basename, it) }
                        .findFirst()

                    // If we were unable to find a configuration, i.e. we are
                    // unable to link the item we have to return false.
                    if (!value.isPresent) {
                        return@filter true
                    }
                    val linkMap = value.get()

                    // Build the path for the link and target.
                    val link = Paths.get(linkMap.target, item.basename)
                    val target = Paths.get(linkMap.prefix, this.basename, item.basename)
                    !createSymbolicLink(Link(link, target))
                }
                .collect(Collectors.toList())

            // If the containing items have been linked we can filter the group.
            //
            // We need to update the items to only list unlinked items.
            this.nodes = items
            return this.nodes.isEmpty()
        }
    }

    data class Leaf(override val path: Path) : Node() {
        override fun link(linkMaps: Set<LinkMap>): Boolean {
            val link = match(this, linkMaps) ?: return false

            return createSymbolicLink(Link(link.path, link.source))
        }
    }

    data class Link(override val path: Path, val source: Path) : Node() {
        override fun link(linkMaps: Set<LinkMap>): Boolean {
            val link = match(this, linkMaps) ?: return false

            return createSymbolicLink(Link(link.path, link.source))
        }
    }
}
