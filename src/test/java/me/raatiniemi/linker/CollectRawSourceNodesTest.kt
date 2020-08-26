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

import me.raatiniemi.linker.Main.collectRawSourceNodes
import me.raatiniemi.linker.domain.Item
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path
import java.nio.file.Paths

@RunWith(JUnit4::class)
class CollectRawSourceNodesTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `collect raw source nodes with empty directory`() {
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = emptyMap<Path, List<String>>()

        val actual = collectRawSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect raw source nodes with single empty directory`() {
        createNewFolder(temporaryFolder, "folder")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = hashMapOf<Path, List<Item>>(
            Paths.get(temporaryFolder.root.absolutePath, "/folder") to emptyList()
        )

        val actual = collectRawSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect raw source nodes with single directory and child`() {
        createNewFolder(temporaryFolder, "folder/folder-1")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = hashMapOf(
            Paths.get(temporaryFolder.root.absolutePath, "/folder") to listOf(
                Item(Paths.get("folder-1"))
            )
        )

        val actual = collectRawSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect raw source nodes with single directory and children`() {
        createNewFolder(temporaryFolder, "folder/folder-1")
        createNewFolder(temporaryFolder, "folder/folder-2")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = hashMapOf(
            Paths.get(temporaryFolder.root.absolutePath, "/folder") to listOf(
                Item(Paths.get("folder-1")),
                Item(Paths.get("folder-2"))
            )
        )

        val actual = collectRawSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect raw source nodes with empty directories`() {
        createNewFolder(temporaryFolder, "folder-1")
        createNewFolder(temporaryFolder, "folder-2")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = hashMapOf<Path, List<Item>>(
            Paths.get(temporaryFolder.root.absolutePath, "/folder-1") to emptyList(),
            Paths.get(temporaryFolder.root.absolutePath, "/folder-2") to emptyList()
        )

        val actual = collectRawSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect raw source nodes with directories and child`() {
        createNewFolder(temporaryFolder, "folder/folder")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = hashMapOf<Path, List<Item>>(
            Paths.get(temporaryFolder.root.absolutePath, "/folder") to listOf(
                Item(Paths.get("folder"))
            )
        )

        val actual = collectRawSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect raw source nodes with directories and children`() {
        createNewFolder(temporaryFolder, "folder-1/folder-1")
        createNewFolder(temporaryFolder, "folder-1/folder-2")
        createNewFolder(temporaryFolder, "folder-2/folder-1")
        createNewFolder(temporaryFolder, "folder-2/folder-2")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = emptyList<String>()
        val expected = hashMapOf<Path, List<Item>>(
            Paths.get(temporaryFolder.root.absolutePath, "/folder-1") to listOf(
                Item(Paths.get("folder-1")),
                Item(Paths.get("folder-2"))
            ),
            Paths.get(temporaryFolder.root.absolutePath, "/folder-2") to listOf(
                Item(Paths.get("folder-1")),
                Item(Paths.get("folder-2"))
            )
        )

        val actual = collectRawSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect raw source nodes with excludes`() {
        createNewFolder(temporaryFolder, "folder-1/folder-1")
        createNewFolder(temporaryFolder, "folder-1/folder-2")
        createNewFolder(temporaryFolder, "folder-2/folder-1")
        createNewFolder(temporaryFolder, "folder-2/folder-2")
        val path = temporaryFolder.root.toPath()
        val excludeDirectories = listOf(
            "folder-2"
        )
        val expected = hashMapOf<Path, List<Item>>(
            Paths.get(temporaryFolder.root.absolutePath, "/folder-1") to listOf(
                Item(Paths.get("folder-1"))
            ),
            Paths.get(temporaryFolder.root.absolutePath, "/folder-2/folder-1") to emptyList()
        )

        val actual = collectRawSourceNodes(path, excludeDirectories)

        assertEquals(expected, actual)
    }
}
