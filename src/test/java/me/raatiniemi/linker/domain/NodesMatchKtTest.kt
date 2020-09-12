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

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Paths

@RunWith(JUnit4::class)
internal class NodesMatchKtTest {
    // Leaf

    @Test
    fun `match leaf without link maps`() {
        val linkMaps = emptySet<LinkMap>()
        val node = Node.Leaf(
            path = Paths.get("sources", "folder")
        )
        val expected: Node.Link? = null

        val actual = match(node, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `match leaf without matching link map`() {
        val linkMaps = setOf(
            LinkMap(
                regex = "regex",
                prefix = "prefix",
                target = "target"
            )
        )
        val node = Node.Leaf(
            path = Paths.get("sources", "folder")
        )
        val expected: Node.Link? = null

        val actual = match(node, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `match leaf with matching link map`() {
        val linkMaps = setOf(
            LinkMap(
                regex = "folder",
                prefix = "sources",
                target = "targets"
            )
        )
        val node = Node.Leaf(
            path = Paths.get("sources", "folder")
        )
        val expected = Node.Link(
            path = Paths.get("targets", "folder"),
            source = Paths.get("sources", "folder")
        )

        val actual = match(node, linkMaps)

        assertEquals(expected, actual)
    }

    // Link

    @Test
    fun `match link without link maps`() {
        val linkMaps = emptySet<LinkMap>()
        val node = Node.Link(
            path = Paths.get("sources", "folder-2"),
            source = Paths.get("sources", "folder-1")
        )
        val expected: Node.Link? = null

        val actual = match(node, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `match link without matching link map`() {
        val linkMaps = setOf(
            LinkMap(
                regex = "regex",
                prefix = "prefix",
                target = "target"
            )
        )
        val node = Node.Link(
            path = Paths.get("sources", "folder-2"),
            source = Paths.get("sources", "folder-1")
        )
        val expected: Node.Link? = null

        val actual = match(node, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `match link with matching link map`() {
        val linkMaps = setOf(
            LinkMap(
                regex = "folder-2",
                prefix = "sources",
                target = "targets"
            )
        )
        val node = Node.Link(
            path = Paths.get("sources", "folder-2"),
            source = Paths.get("sources", "folder-1")
        )
        val expected = Node.Link(
            path = Paths.get("targets", "folder-2"),
            source = Paths.get("sources", "folder-2")
        )

        val actual = match(node, linkMaps)

        assertEquals(expected, actual)
    }
}
