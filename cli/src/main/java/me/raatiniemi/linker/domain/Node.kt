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

/**
 * Represents the different states for a given path.
 */
internal sealed class Node {
    abstract val path: Path

    val basename: String
        get() = path.fileName.toString()

    data class Branch(override val path: Path, var nodes: List<Node>) : Node()

    data class Leaf(override val path: Path) : Node()

    data class Link(override val path: Path, val source: Path) : Node()
}
