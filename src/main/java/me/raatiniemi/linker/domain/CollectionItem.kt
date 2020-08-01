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

import me.raatiniemi.linker.filter.excludeFilter
import me.raatiniemi.linker.util.createSymbolicLink
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

internal class CollectionItem(path: Path, private var items: List<Item>) : Item(path) {
    /**
     * @inheritDoc
     */
    override fun filter(data: List<Directory>): Boolean {
        // Check whether the group have been linked. If the group is linked
        // there is no need to check the subdirectories.
        if (!super.filter(data)) {
            return false
        }

        // The group have not been linked, we have to check if the contained
        // items have been linked.
        val items = items.stream()
            .filter { item ->
                excludeFilter(item, data)
            }
            .collect(Collectors.toList())

        // If the containing items have been linked we can filter the group.
        //
        // We need to update the items to only list unlinked items.
        this.items = items
        return this.items.isNotEmpty()
    }

    /**
     * @inheritDoc
     */
    override fun link(linkMaps: Set<LinkMap>): Boolean {
        val items = items.stream()
            .filter { item: Item ->
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
        this.items = items
        return this.items.isEmpty()
    }

    override fun toString(): String {
        var value = super.toString()

        // If the group contain any items they should be appended to the value.
        //
        // Directory 1
        // Directory 2 (CollectionItem)
        //     Directory 3
        //     Directory 4
        val items = items
            .stream()
            .map { obj: Item -> obj.basename }
            .collect(Collectors.toList())

        if (items.isNotEmpty()) {
            val separator = "\n  "
            value += separator + java.lang.String.join(separator, items)
        }
        return value
    }
}
