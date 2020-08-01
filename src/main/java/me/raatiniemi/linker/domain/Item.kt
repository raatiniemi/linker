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
import java.util.*

internal open class Item(override val path: Path) : Directory {
    override val basename: String
        get() = path.fileName.toString()

    /**
     * @inheritDoc
     */
    override fun filter(data: List<Directory>): Boolean {
        return excludeFilter(this, data)
    }

    /**
     * @inheritDoc
     */
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

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        // Since we are doing comparison based on the basename it doesn't
        // really matter whether the object is a CollectionItem or an Item.
        if (other !is Directory) {
            return false
        }

        // Do basic comparison based on the directory basename.
        //
        // TODO: Use absolute path instead?
        // If the directory names aren't unique comparing only the basename
        // will give false positives.
        return (this.basename == other.basename)
    }

    override fun hashCode(): Int {
        var result = 17
        result = 31 * result + Objects.hashCode(basename)
        return result
    }

    override fun toString(): String {
        return this.basename
    }
}
