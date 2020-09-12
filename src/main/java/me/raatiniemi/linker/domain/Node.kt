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

import me.raatiniemi.linker.util.createSymbolicLink
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
                    val linkMap = linkMaps.stream()
                        .filter { map: LinkMap -> map.match(item.basename) }
                        .findFirst()

                    // If we were unable to find a configuration, i.e. we are
                    // unable to link the item we have to return false.
                    if (!linkMap.isPresent) {
                        return@filter true
                    }
                    val map = linkMap.get()

                    // Build the path for the link and target.
                    val link = Paths.get(map.target, item.basename)
                    val target = Paths.get(map.prefix, this.basename, item.basename)
                    !createSymbolicLink(link, target)
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
            // Attempt to find a link map configuration based on the basename.
            val linkMap = linkMaps.stream()
                .filter { map: LinkMap -> map.match(this.basename) }
                .findFirst()

            // If we were unable to find a configuration, i.e. we are unable to
            // link the item we have to return false.
            if (!linkMap.isPresent) {
                return false
            }
            val map = linkMap.get()

            // Build the path for the link and target.
            val link = Paths.get(map.target, this.basename)
            val target = Paths.get(map.prefix, this.basename)

            // If the symbolic link is created we have to exclude the item from the
            // filter by returning false.
            return createSymbolicLink(link, target)
        }
    }

    data class Link(override val path: Path, val source: Path) : Node() {
        override fun link(linkMaps: Set<LinkMap>): Boolean {
            // Attempt to find a link map configuration based on the basename.
            val linkMap = linkMaps.stream()
                .filter { map: LinkMap -> map.match(this.basename) }
                .findFirst()

            // If we were unable to find a configuration, i.e. we are unable to
            // link the item we have to return false.
            if (!linkMap.isPresent) {
                return false
            }
            val map = linkMap.get()

            // Build the path for the link and target.
            val link = Paths.get(map.target, this.basename)
            val target = Paths.get(map.prefix, this.basename)

            // If the symbolic link is created we have to exclude the item from the
            // filter by returning false.
            return createSymbolicLink(link, target)
        }
    }
}
