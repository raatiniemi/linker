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

internal interface Directory {
    /**
     * Getter method for the path of the directory.
     *
     * @return Path of the directory.
     */
    val path: Path

    /**
     * Getter method for the basename of the directory.
     *
     * @return Basename for the directory.
     */
    val basename: String

    /**
     * Filter item based on the data.
     *
     * @param data Data source.
     * @return false if item is found within data, otherwise true.
     */
    fun filter(data: List<Directory>): Boolean

    /**
     * Attempt to link directory if link map configuration is found.
     *
     * @param linkMaps Link map configurations.
     * @return true if item was linked, otherwise false.
     */
    fun link(linkMaps: Set<LinkMap>): Boolean
}
