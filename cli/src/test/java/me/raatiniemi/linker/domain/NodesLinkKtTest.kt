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

import me.raatiniemi.linker.createNewFile
import me.raatiniemi.linker.createNewFolder
import me.raatiniemi.linker.getPath
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NodesLinkKtTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    // Link

    @Test
    fun `link without nodes`() {
        val nodes = emptyList<Node>()
        val linkMaps = setOf(
            LinkMap(
                regex = "folder",
                target = "targets"
            )
        )
        val expected = emptyList<Node>()

        val actual = link(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `link without link maps`() {
        val nodes = emptyList<Node>()
        val linkMaps = emptySet<LinkMap>()
        val expected = emptyList<Node>()

        val actual = link(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `link without matching link map`() {
        val source = createNewFile(temporaryFolder, "sources/folder/leaf")
        createNewFolder(temporaryFolder, "targets")
        val linkMaps = setOf(
            LinkMap(
                "without match",
                "${temporaryFolder.root.absolutePath}/targets"
            )
        )
        val nodes = listOf(
            Node.Branch(
                path = getPath(temporaryFolder, "sources"),
                nodes = listOf(
                    Node.Branch(
                        path = getPath(temporaryFolder, "sources", "folder"),
                        nodes = listOf(
                            Node.Leaf(source)
                        )
                    )
                )
            )
        )
        val expected = listOf(
            Node.Branch(
                path = getPath(temporaryFolder, "sources"),
                nodes = listOf(
                    Node.Branch(
                        path = getPath(temporaryFolder, "sources", "folder"),
                        nodes = listOf(
                            Node.Leaf(source)
                        )
                    )
                )
            )
        )

        val actual = link(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `link with link map for leaf`() {
        val source = createNewFile(temporaryFolder, "sources/folder/leaf")
        createNewFolder(temporaryFolder, "targets")
        val linkMaps = setOf(
            LinkMap(
                "leaf",
                "${temporaryFolder.root.absolutePath}/targets"
            )
        )
        val nodes = listOf(
            Node.Branch(
                path = getPath(temporaryFolder, "sources"),
                nodes = listOf(
                    Node.Branch(
                        path = getPath(temporaryFolder, "sources", "folder"),
                        nodes = listOf(
                            Node.Leaf(source)
                        )
                    )
                )
            )
        )
        val expected = emptyList<Node>()

        val actual = link(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `link with link map for parent`() {
        val source = createNewFile(temporaryFolder, "sources/folder/leaf")
        createNewFolder(temporaryFolder, "targets")
        val linkMaps = setOf(
            LinkMap(
                "folder",
                "${temporaryFolder.root.absolutePath}/targets"
            )
        )
        val nodes = listOf(
            Node.Branch(
                path = getPath(temporaryFolder, "sources"),
                nodes = listOf(
                    Node.Branch(
                        path = getPath(temporaryFolder, "sources", "folder"),
                        nodes = listOf(
                            Node.Leaf(source)
                        )
                    )
                )
            )
        )
        val expected = emptyList<Node>()

        val actual = link(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    // Dry run link

    @Test
    fun `dry run link without nodes`() {
        val nodes = emptyList<Node>()
        val linkMaps = setOf(
            LinkMap(
                regex = "folder",
                target = "targets"
            )
        )
        val expected = emptyList<Node>()

        val actual = dryRunLink(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `dry run link without link maps`() {
        val nodes = emptyList<Node>()
        val linkMaps = emptySet<LinkMap>()
        val expected = emptyList<Node>()

        val actual = dryRunLink(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `dry run link without matching link map`() {
        val source = createNewFile(temporaryFolder, "sources/folder/leaf")
        createNewFolder(temporaryFolder, "targets")
        val linkMaps = setOf(
            LinkMap(
                "without match",
                "${temporaryFolder.root.absolutePath}/targets"
            )
        )
        val nodes = listOf(
            Node.Branch(
                path = getPath(temporaryFolder, "sources"),
                nodes = listOf(
                    Node.Branch(
                        path = getPath(temporaryFolder, "sources", "folder"),
                        nodes = listOf(
                            Node.Leaf(source)
                        )
                    )
                )
            )
        )
        val expected = listOf(
            Node.Branch(
                path = getPath(temporaryFolder, "sources"),
                nodes = listOf(
                    Node.Branch(
                        path = getPath(temporaryFolder, "sources", "folder"),
                        nodes = listOf(
                            Node.Leaf(source)
                        )
                    )
                )
            )
        )

        val actual = dryRunLink(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `dry run link with link map for leaf`() {
        val source = createNewFile(temporaryFolder, "sources/folder/leaf")
        createNewFolder(temporaryFolder, "targets")
        val linkMaps = setOf(
            LinkMap(
                "leaf",
                "${temporaryFolder.root.absolutePath}/targets"
            )
        )
        val nodes = listOf(
            Node.Branch(
                path = getPath(temporaryFolder, "sources"),
                nodes = listOf(
                    Node.Branch(
                        path = getPath(temporaryFolder, "sources", "folder"),
                        nodes = listOf(
                            Node.Leaf(source)
                        )
                    )
                )
            )
        )
        val expected = emptyList<Node>()

        val actual = link(nodes, linkMaps)

        assertEquals(expected, actual)
    }

    @Test
    fun `dry run link with link map for parent`() {
        val source = createNewFile(temporaryFolder, "sources/folder/leaf")
        createNewFolder(temporaryFolder, "targets")
        val linkMaps = setOf(
            LinkMap(
                "folder",
                "${temporaryFolder.root.absolutePath}/targets"
            )
        )
        val nodes = listOf(
            Node.Branch(
                path = getPath(temporaryFolder, "sources"),
                nodes = listOf(
                    Node.Branch(
                        path = getPath(temporaryFolder, "sources", "folder"),
                        nodes = listOf(
                            Node.Leaf(source)
                        )
                    )
                )
            )
        )
        val expected = emptyList<Node>()

        val actual = link(nodes, linkMaps)

        assertEquals(expected, actual)
    }
}
