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
import me.raatiniemi.linker.domain.createSymbolicLink
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CollectTargetNodesKtTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `collect target nodes without targets`() {
        val targets = emptyList<String>()
        val expected = emptyList<Node.Link>()

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with empty target`() {
        val targets = listOf(
            temporaryFolder.root.absolutePath
        )
        val expected = emptyList<Node.Link>()

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with file in target`() {
        createNewFile(temporaryFolder, "file-in-target")
        val targets = listOf(
            temporaryFolder.root.absolutePath
        )
        val expected = emptyList<Node.Link>()

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with directory in target`() {
        createNewFolder(temporaryFolder, "directory-in-target")
        val targets = listOf(
            temporaryFolder.root.absolutePath
        )
        val expected = emptyList<Node.Link>()

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with node linked to source`() {
        val source = createNewFile(temporaryFolder, "source/file")
        val target = createNewFolder(temporaryFolder, "target")
        val link = getPath(temporaryFolder, "target", "file")
        createSymbolicLink(Node.Link(link, source))
        val targets = listOf(
            target.toString()
        )
        val expected = listOf(
            Node.Link(link, source)
        )

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with node linked to source in nested folders`() {
        val source = createNewFile(temporaryFolder, "source/folder/file")
        val target = createNewFolder(temporaryFolder, "target")
        createNewFolder(temporaryFolder, "target/folder/subfolder")
        val link = getPath(temporaryFolder, "target", "folder", "subfolder", "file")
        createSymbolicLink(Node.Link(link, source))
        val targets = listOf(
            target.toString()
        )
        val expected = listOf(
            Node.Link(link, source)
        )

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }
}
