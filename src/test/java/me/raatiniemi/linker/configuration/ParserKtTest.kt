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

package me.raatiniemi.linker.configuration

import me.raatiniemi.linker.domain.LinkMap
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ParserKtTest {
    @get:Rule
    var folder = TemporaryFolder()

    @Test(expected = RuntimeException::class)
    fun `parse without file`() {
        val basename = "configuration.json"
        val filename = "${folder.root.path}/$basename"

        parseConfiguration(filename)
    }

    @Test(expected = RuntimeException::class)
    fun `parse with empty configuration`() {
        val basename = "configuration.json"
        val filename = "${folder.root.path}/$basename"
        folder.newFile(basename)

        parseConfiguration(filename)
    }

    @Test(expected = RuntimeException::class)
    fun `parse without key for source`() {
        val basename = "configuration.json"
        val filename = "${folder.root.path}/$basename"
        val file = folder.newFile(basename)
        file.writeText(
            """
            {
            }
            """.trimIndent()
        )

        parseConfiguration(filename)
    }

    @Test(expected = RuntimeException::class)
    fun `parse with empty key for source`() {
        val basename = "configuration.json"
        val filename = "${folder.root.path}/$basename"
        val file = folder.newFile(basename)
        file.writeText(
            """
            {
                "source": ""
            }
            """.trimIndent()
        )

        parseConfiguration(filename)
    }

    @Test(expected = RuntimeException::class)
    fun `parse without key for targets`() {
        val basename = "configuration.json"
        val filename = "${folder.root.path}/$basename"
        val file = folder.newFile(basename)
        file.writeText(
            """
            {
                "source": "/tmp"
            }
            """.trimIndent()
        )

        parseConfiguration(filename)
    }

    @Test(expected = RuntimeException::class)
    fun `parse with empty key for targets`() {
        val basename = "configuration.json"
        val filename = "${folder.root.path}/$basename"
        val file = folder.newFile(basename)
        file.writeText(
            """
            {
                "source": "/tmp",
                "targets": []
            }
            """.trimIndent()
        )

        parseConfiguration(filename)
    }

    @Test
    fun `parse with required configuration`() {
        val basename = "configuration.json"
        val filename = "${folder.root.path}/$basename"
        val file = folder.newFile(basename)
        file.writeText(
            """
            {
                "source": "/var/cache/pacman/pkg",
                "targets": [
                    "/var/www/archlinux/pkg"
                ]
            }
            """.trimIndent()
        )
        val expected = Configuration(
            "/var/cache/pacman/pkg",
            listOf(
                "/var/www/archlinux/pkg"
            ),
            emptyList(),
            emptySet()
        )

        val actual = parseConfiguration(filename)

        assertEquals(expected, actual)
    }

    @Test
    fun `parse with configuration`() {
        val basename = "configuration.json"
        val filename = "${folder.root.path}/$basename"
        val file = folder.newFile(basename)
        file.writeText(
            """
            {
                "source": "/var/cache/pacman/pkg",
                "targets": [
                    "/var/www/archlinux/pkg"
                ],
                "excludes": [
                    "*.zip"
                ],
                "linkMaps": [
                    {
                        "regex": "(.*)\\.pkg\\.tar\\.xz",
                        "prefix": "/var/cache/pacman/pkg",
                        "target": "/var/www/archlinux/pkg"
                    }
                ]
            }
            """.trimIndent()
        )
        val expected = Configuration(
            "/var/cache/pacman/pkg",
            listOf(
                "/var/www/archlinux/pkg"
            ),
            listOf(
                "*.zip"
            ),
            setOf(
                LinkMap(
                    "(.*)\\.pkg\\.tar\\.xz",
                    "/var/cache/pacman/pkg",
                    "/var/www/archlinux/pkg"
                )
            )
        )

        val actual = parseConfiguration(filename)

        assertEquals(expected, actual)
    }
}
