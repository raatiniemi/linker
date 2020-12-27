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

package me.raatiniemi.linker

import me.raatiniemi.linker.domain.Node
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Paths

@RunWith(JUnit4::class)
class CollectNodesKtTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `collect nodes with empty path`() {
        val expected = emptyList<Node>()

        val actual = collectNodes(temporaryFolder.root)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect nodes containing node`() {
        val node = createNewFile(temporaryFolder, "node")
        val expected = listOf(
            Node.Leaf(node)
        )

        val actual = collectNodes(temporaryFolder.root)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect nodes containing nodes`() {
        val firstNode = createNewFile(temporaryFolder, "node-1")
        val secondNode = createNewFile(temporaryFolder, "node-2")
        val expected = listOf(
            Node.Leaf(firstNode),
            Node.Leaf(secondNode)
        )

        val actual = collectNodes(temporaryFolder.root)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect nodes containing nested node`() {
        val nodes = getPath(temporaryFolder, "nodes")
        val node = createNewFile(temporaryFolder, "nodes/node")
        val expected = listOf(
            Node.Branch(
                nodes,
                listOf(
                    Node.Leaf(node)
                )
            )
        )

        val actual = collectNodes(temporaryFolder.root)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect nodes containing nested nodes`() {
        val firstLevel = getPath(temporaryFolder, "first-level")
        val secondLevel = getPath(temporaryFolder, "first-level", "second-level")
        val node = createNewFile(temporaryFolder, "first-level/second-level/node")
        val expected = listOf(
            Node.Branch(
                firstLevel,
                listOf(
                    Node.Branch(
                        secondLevel,
                        listOf(
                            Node.Leaf(node)
                        )
                    )
                )
            )
        )

        val actual = collectNodes(temporaryFolder.root)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect nodes with symbolic link node`() {
        val link = getPath(temporaryFolder, "link")
        val leaf = createNewFile(temporaryFolder, "leaf")
        Files.createSymbolicLink(link, leaf)
        val expected = listOf(
            Node.Leaf(leaf),
            Node.Link(link, leaf)
        )

        val actual = collectNodes(temporaryFolder.root)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect nodes with relative symbolic link node`() {
        createNewFolder(temporaryFolder, "targets")
        val link = getPath(temporaryFolder, "targets", "link")
        val leaf = createNewFile(temporaryFolder, "sources/leaf")
        Files.createSymbolicLink(link, Paths.get("..", "sources", "leaf"))
        val expected = listOf(
            Node.Branch(
                getPath(temporaryFolder, "sources"),
                listOf(
                    Node.Leaf(leaf)
                )
            ),
            Node.Branch(
                getPath(temporaryFolder, "targets"),
                listOf(
                    Node.Link(link, leaf)
                )
            )
        )

        val actual = collectNodes(temporaryFolder.root)

        assertEquals(expected, actual)
    }
}
