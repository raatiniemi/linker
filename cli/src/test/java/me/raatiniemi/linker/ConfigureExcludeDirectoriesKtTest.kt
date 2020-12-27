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

import me.raatiniemi.linker.configuration.Configuration
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ConfigureExcludeDirectoriesKtTest {
    @Test
    fun `configure exclude directories without excludes`() {
        val configuration = Configuration(
            source = "",
            targets = emptyList()
        )
        val expected = emptyList<String>()

        val actual = configureExcludeDirectories(configuration)

        assertEquals(expected, actual)
    }

    @Test
    fun `configure exclude directories with exclude`() {
        val configuration = Configuration(
            source = "",
            targets = emptyList(),
            excludes = listOf(
                "/tmp"
            )
        )
        val expected = listOf(
            "/tmp"
        )

        val actual = configureExcludeDirectories(configuration)

        assertEquals(expected, actual)
    }

    @Test
    fun `configure exclude directories with uppercase exclude`() {
        val configuration = Configuration(
            source = "",
            targets = emptyList(),
            excludes = listOf(
                "/TMP"
            )
        )
        val expected = listOf(
            "/tmp"
        )

        val actual = configureExcludeDirectories(configuration)

        assertEquals(expected, actual)
    }

    @Test
    fun `configure exclude directories with excludes`() {
        val configuration = Configuration(
            source = "",
            targets = emptyList(),
            excludes = listOf(
                "/var/tmp",
                "/tmp"
            )
        )
        val expected = listOf(
            "/var/tmp",
            "/tmp"
        )

        val actual = configureExcludeDirectories(configuration)

        assertEquals(expected, actual)
    }
}
