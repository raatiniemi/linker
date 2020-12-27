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
class NodesFilterKtTest {
    @Test
    fun `filter with empty sources and targets`() {
        val sources = emptyList<Node>()
        val targets = emptyList<Node.Link>()
        val expected = emptyList<Node>()

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }

    // Branch

    @Test
    fun `filter with source branch`() {
        val sources = listOf<Node>(
            Node.Branch(
                Paths.get("sources", "branch"),
                emptyList()
            )
        )
        val targets = emptyList<Node.Link>()
        val expected = listOf(
            Node.Branch(
                Paths.get("sources", "branch"),
                emptyList()
            )
        )

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `filter with nested source branch`() {
        val sources = listOf<Node>(
            Node.Branch(
                Paths.get("sources", "branch"),
                listOf(
                    Node.Branch(
                        Paths.get("sources", "branch", "folder"),
                        emptyList()
                    )
                )
            )
        )
        val targets = emptyList<Node.Link>()
        val expected = listOf(
            Node.Branch(
                Paths.get("sources", "branch"),
                listOf(
                    Node.Branch(
                        Paths.get("sources", "branch", "folder"),
                        emptyList()
                    )
                )
            )
        )

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `filter with linked source branch`() {
        val sources = listOf<Node>(
            Node.Branch(
                Paths.get("sources", "branch"),
                emptyList()
            )
        )
        val targets = listOf(
            Node.Link(
                Paths.get("targets", "branch"),
                Paths.get("sources", "branch")
            )
        )
        val expected = emptyList<Node>()

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `filter with nested linked source branch`() {
        val sources = listOf<Node>(
            Node.Branch(
                Paths.get("sources", "branch"),
                listOf(
                    Node.Branch(
                        Paths.get("sources", "branch", "folder"),
                        emptyList()
                    )
                )
            )
        )
        val targets = listOf(
            Node.Link(
                Paths.get("targets", "branch"),
                Paths.get("sources", "branch", "folder")
            )
        )
        val expected = emptyList<Node>()

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `filter with relative linked source branch`() {
        val sources = listOf<Node>(
            Node.Branch(
                Paths.get("sources", "branch"),
                emptyList()
            )
        )
        val targets = listOf(
            Node.Link(
                Paths.get("targets", "branch"),
                Paths.get("targets", "..", "sources", "branch")
            )
        )
        val expected = emptyList<Node>()

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }

    // Leaf

    @Test
    fun `filter with source leaf`() {
        val sources = listOf<Node>(
            Node.Leaf(Paths.get("sources", "leaf"))
        )
        val targets = emptyList<Node.Link>()
        val expected = listOf(
            Node.Leaf(Paths.get("sources", "leaf"))
        )

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `filter with linked source leaf`() {
        val sources = listOf<Node>(
            Node.Leaf(Paths.get("sources", "leaf"))
        )
        val targets = listOf(
            Node.Link(
                Paths.get("targets", "leaf"),
                Paths.get("sources", "leaf")
            )
        )
        val expected = emptyList<Node>()

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }

    // Link

    @Test
    fun `filter with source link`() {
        val sources = listOf<Node>(
            Node.Link(
                Paths.get("sources", "link"),
                Paths.get("sources", "folder")
            )
        )
        val targets = emptyList<Node.Link>()
        val expected = listOf(
            Node.Link(
                Paths.get("sources", "link"),
                Paths.get("sources", "folder")
            )
        )

        val actual = filter(sources, targets)

        assertEquals(expected, actual)
    }
}
