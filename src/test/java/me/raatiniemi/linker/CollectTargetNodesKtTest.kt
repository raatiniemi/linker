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

import me.raatiniemi.linker.domain.Directory
import me.raatiniemi.linker.domain.Item
import me.raatiniemi.linker.util.createSymbolicLink
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Paths

@RunWith(JUnit4::class)
class CollectTargetNodesKtTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `collect target nodes without targets`() {
        val targets = emptyList<String>()
        val expected = emptyList<Directory>()

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with empty target`() {
        val targets = listOf(
            temporaryFolder.root.absolutePath
        )
        val expected = emptyList<Directory>()

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with file in target`() {
        temporaryFolder.newFile("file-in-target")
        val targets = listOf(
            temporaryFolder.root.absolutePath
        )
        val expected = emptyList<Directory>()

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with directory in target`() {
        temporaryFolder.newFolder("directory-in-target")
        val targets = listOf(
            temporaryFolder.root.absolutePath
        )
        val expected = emptyList<Directory>()

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }

    @Test
    fun `collect target nodes with node linked to source`() {
        val source = temporaryFolder.newFolder("source")
        val sourceFile = Paths.get(source.path, "file")
            .also { it.toFile().createNewFile() }
        val target = temporaryFolder.newFolder("target")
        val targetFile = Paths.get(target.path, "file")
        createSymbolicLink(targetFile, sourceFile)
        val targets = listOf(
            target.absolutePath
        )
        val expected = listOf(
            Item(targetFile)
        )

        val actual = collectTargetNodes(targets)

        assertEquals(expected, actual)
    }
}
