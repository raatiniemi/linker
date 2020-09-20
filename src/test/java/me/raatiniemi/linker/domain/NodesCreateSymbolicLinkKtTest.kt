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

import me.raatiniemi.linker.createNewFolder
import me.raatiniemi.linker.getPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files

@RunWith(JUnit4::class)
class NodesCreateSymbolicLinkKtTest {
    @get:Rule
    var temporaryFolder = TemporaryFolder()

    @Test
    fun `create symbolic link`() {
        val source = createNewFolder(temporaryFolder, "sources")
        val target = getPath(temporaryFolder, "targets")
        val node = Node.Link(target, source)
        val expected = true

        val actual = createSymbolicLink(node)

        assertEquals(expected, actual)
        assertTrue("Symbolic link do not exists", target.toFile().exists())
        assertEquals(source, Files.readSymbolicLink(target))
    }

    @Test
    fun `create symbolic link when parent directory do not exists`() {
        val source = createNewFolder(temporaryFolder, "sources")
        val target = getPath(temporaryFolder, "targets/folder")
        val node = Node.Link(target, source)
        val expected = true

        val actual = createSymbolicLink(node)

        assertEquals(expected, actual)
        assertTrue("Symbolic link do not exists", target.toFile().exists())
        assertEquals(source, Files.readSymbolicLink(target))
    }
}
