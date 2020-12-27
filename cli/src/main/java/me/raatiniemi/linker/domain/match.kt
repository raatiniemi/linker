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

import java.nio.file.Paths
import java.util.regex.Pattern

internal fun match(node: Node, linkMaps: Set<LinkMap>): Node.Link? {
    return linkMaps.firstOrNull { match(node.basename, it) }
        ?.let {
            val link = Paths.get(it.target, node.basename)
            Node.Link(link, node.path)
        }
}

internal fun match(value: String, linkMap: LinkMap): Boolean {
    if (value.isBlank()) {
        return false
    }

    return Pattern.matches(linkMap.regex, value)
}
