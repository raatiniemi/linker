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
class CollectSourceNodesKtTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `collect source nodes with empty directory`() {
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = emptyList<Node>()

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    // Directory

    @Test
    fun `collect source nodes with single empty directory`() {
        val node = createNewFolder(temporaryFolder, "folder")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Branch(node, emptyList())
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with single directory and child`() {
        val child = createNewFolder(temporaryFolder, "folder/folder-1")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder"),
                listOf(
                    Node.Branch(child, emptyList())
                )
            )
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with single directory and children`() {
        val firstChild = createNewFolder(temporaryFolder, "folder/folder-1")
        val secondChild = createNewFolder(temporaryFolder, "folder/folder-2")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder"),
                listOf(
                    Node.Branch(firstChild, emptyList()),
                    Node.Branch(secondChild, emptyList())
                )
            )
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with empty directories`() {
        createNewFolder(temporaryFolder, "folder-1")
        createNewFolder(temporaryFolder, "folder-2")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder-1"),
                emptyList()
            ),
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder-2"),
                emptyList()
            )
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with directories and child`() {
        val firstChild = createNewFolder(temporaryFolder, "folder-1/folder")
        val secondChild = createNewFolder(temporaryFolder, "folder-2/folder")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder-1"),
                listOf(
                    Node.Branch(firstChild, emptyList())
                )
            ),
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder-2"),
                listOf(
                    Node.Branch(secondChild, emptyList())
                )
            )
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with directories and children`() {
        val firstChild = createNewFolder(temporaryFolder, "folder-1/folder-1")
        val secondChild = createNewFolder(temporaryFolder, "folder-1/folder-2")
        val thirdChild = createNewFolder(temporaryFolder, "folder-2/folder-3")
        val fourthChild = createNewFolder(temporaryFolder, "folder-2/folder-4")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder-1"),
                listOf(
                    Node.Branch(firstChild, emptyList()),
                    Node.Branch(secondChild, emptyList())
                )
            ),
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder-2"),
                listOf(
                    Node.Branch(thirdChild, emptyList()),
                    Node.Branch(fourthChild, emptyList())
                )
            )
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes when excluding directory`() {
        val child = createNewFolder(temporaryFolder, "folder-1")
        createNewFolder(temporaryFolder, "folder-2")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = listOf(
            "folder-2"
        )
        val expected = listOf(
            Node.Branch(child, emptyList())
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes when excluding child directory`() {
        val child = createNewFolder(temporaryFolder, "folder-1/folder-1")
        createNewFolder(temporaryFolder, "folder-1/folder-2")
        createNewFolder(temporaryFolder, "folder-2/folder-1")
        createNewFolder(temporaryFolder, "folder-2/folder-2")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = listOf(
            "folder-2"
        )
        val expected = listOf(
            Node.Branch(
                Paths.get(temporaryFolder.root.absolutePath, "folder-1"),
                listOf(
                    Node.Branch(child, emptyList())
                )
            )
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    // File

    @Test
    fun `collect source nodes with single file`() {
        val path = temporaryFolder.root.toPath()
        val node = createNewFile(temporaryFolder, "file")
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Leaf(node)
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with multiple files`() {
        val path = temporaryFolder.root.toPath()
        val first = createNewFile(temporaryFolder, "first")
        val second = createNewFile(temporaryFolder, "second")
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Leaf(first),
            Node.Leaf(second)
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with file exclusion`() {
        val path = temporaryFolder.root.toPath()
        val first = createNewFile(temporaryFolder, "first")
        createNewFile(temporaryFolder, "second")
        val excludeDirectories = listOf(
            "second"
        )
        val expected = listOf(
            Node.Leaf(first)
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    // Link

    @Test
    fun `collect source nodes with single link`() {
        val path = temporaryFolder.root.toPath()
        val leaf = createNewFile(temporaryFolder, "leaf")
        val link = getPath(temporaryFolder, "link")
        Files.createSymbolicLink(link, leaf)
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Leaf(leaf),
            Node.Link(link, leaf)
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with multiple link`() {
        val path = temporaryFolder.root.toPath()
        val firstLeaf = createNewFile(temporaryFolder, "first-leaf")
        val firstLink = getPath(temporaryFolder, "first-link")
        val secondLeaf = createNewFile(temporaryFolder, "second-leaf")
        val secondLink = getPath(temporaryFolder, "second-link")
        Files.createSymbolicLink(firstLink, firstLeaf)
        Files.createSymbolicLink(secondLink, secondLeaf)
        val excludeDirectories = emptyList<String>()
        val expected = listOf(
            Node.Leaf(firstLeaf),
            Node.Link(firstLink, firstLeaf),
            Node.Leaf(secondLeaf),
            Node.Link(secondLink, secondLeaf)
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect source nodes with link exclusion`() {
        val path = temporaryFolder.root.toPath()
        val firstLeaf = createNewFile(temporaryFolder, "first-leaf")
        val firstLink = getPath(temporaryFolder, "first-link")
        val secondLeaf = createNewFile(temporaryFolder, "second-leaf")
        val secondLink = getPath(temporaryFolder, "second-link")
        Files.createSymbolicLink(firstLink, firstLeaf)
        Files.createSymbolicLink(secondLink, secondLeaf)
        val excludeDirectories = listOf(
            "second-link"
        )
        val expected = listOf(
            Node.Leaf(firstLeaf),
            Node.Link(firstLink, firstLeaf),
            Node.Leaf(secondLeaf)
        )

        val actual = collectSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }
}
