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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Paths

@RunWith(JUnit4::class)
class MainKtTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var configurationBasename: String
    private lateinit var configurationPath: String

    @Before
    fun setUp() {
        configurationBasename = "configuration.json"
        configurationPath = "${temporaryFolder.root.path}/$configurationBasename"
    }

    private fun collectLinkedFiles(): List<Node.Link> {
        return getPath(temporaryFolder, "archlinux")
            .toFile()
            .walkTopDown()
            .map { it.toPath() }
            .filter { Files.isSymbolicLink(it) }
            .filter {
                Files.readSymbolicLink(it)
                    .toFile()
                    .exists()
            }
            .map { path ->
                Node.Link(
                    path,
                    Files.readSymbolicLink(path)
                        .toFile()
                        .canonicalPath
                        .let { Paths.get(it) }
                )
            }
            .toList()
    }

    @Test(expected = IllegalStateException::class)
    fun `main without configuration`() {
        main(emptyArray())
    }

    @Test
    fun `main with full name configuration`() {
        with(temporaryFolder.newFile(configurationBasename)) {
            writeText(
                """
                {
                    "source": "${temporaryFolder.root.path}/pacman",
                    "targets": [
                        "${temporaryFolder.root.path}/archlinux"
                    ],
                    "excludes": [],
                    "linkMaps": [
                        {
                            "regex": "(.*)\\.pkg\\.tar\\.zst",
                            "target": "${temporaryFolder.root.path}/archlinux"
                        }
                    ]
                }
                """.trimIndent()
            )
        }
        val args = arrayOf("--configuration", configurationPath)
        val expected = emptyList<Node.Link>()

        main(args)

        val actual = collectLinkedFiles()
        assertEquals(expected, actual)
    }

    @Test
    fun `main without sources`() {
        with(temporaryFolder.newFile(configurationBasename)) {
            writeText(
                """
                {
                    "source": "${temporaryFolder.root.path}/pacman",
                    "targets": [
                        "${temporaryFolder.root.path}/archlinux"
                    ],
                    "excludes": [],
                    "linkMaps": [
                        {
                            "regex": "(.*)\\.pkg\\.tar\\.zst",
                            "target": "${temporaryFolder.root.path}/archlinux"
                        }
                    ]
                }
                """.trimIndent()
            )
        }
        val args = arrayOf("-c", configurationPath)
        val expected = emptyList<Node.Link>()

        main(args)

        val actual = collectLinkedFiles()
        assertEquals(expected, actual)
    }

    @Test
    fun `main with leaf source`() {
        val file = temporaryFolder.newFile(configurationBasename)
        file.writeText(
            """
            {
                "source": "${temporaryFolder.root.path}/pacman",
                "targets": [
                    "${temporaryFolder.root.path}/archlinux"
                ],
                "excludes": [],
                "linkMaps": [
                    {
                        "regex": "(.*)\\.pkg\\.tar\\.zst",
                        "target": "${temporaryFolder.root.path}/archlinux"
                    }
                ]
            }
            """.trimIndent()
        )
        createNewFile(temporaryFolder, "pacman/name.pkg.tar.zst")
        createNewFolder(temporaryFolder, "archlinux")
        val args = arrayOf("-c", configurationPath)
        val expected = listOf(
            Node.Link(
                getPath(temporaryFolder, "archlinux", "name.pkg.tar.zst"),
                getPath(temporaryFolder, "pacman", "name.pkg.tar.zst")
            )
        )

        main(args)

        val actual = collectLinkedFiles()
        assertEquals(expected, actual)
    }

    @Test
    fun `main with branch source`() {
        val file = temporaryFolder.newFile(configurationBasename)
        file.writeText(
            """
            {
                "source": "${temporaryFolder.root.path}/pacman",
                "targets": [
                    "${temporaryFolder.root.path}/archlinux"
                ],
                "excludes": [],
                "linkMaps": [
                    {
                        "regex": "folder",
                        "target": "${temporaryFolder.root.path}/archlinux"
                    }
                ]
            }
            """.trimIndent()
        )
        createNewFolder(temporaryFolder, "pacman/folder")
        createNewFolder(temporaryFolder, "archlinux")
        val args = arrayOf("-c", configurationPath)
        val expected = listOf(
            Node.Link(
                getPath(temporaryFolder, "archlinux", "folder"),
                getPath(temporaryFolder, "pacman", "folder")
            )
        )

        main(args)

        val actual = collectLinkedFiles()
        assertEquals(expected, actual)
    }

    @Test
    fun `main with nested source`() {
        val file = temporaryFolder.newFile(configurationBasename)
        file.writeText(
            """
            {
                "source": "${temporaryFolder.root.path}/pacman",
                "targets": [
                    "${temporaryFolder.root.path}/archlinux"
                ],
                "excludes": [],
                "linkMaps": [
                    {
                        "regex": "subfolder",
                        "target": "${temporaryFolder.root.path}/archlinux/folder"
                    }
                ]
            }
            """.trimIndent()
        )
        createNewFolder(temporaryFolder, "pacman/folder/subfolder")
        createNewFolder(temporaryFolder, "archlinux/folder")
        val args = arrayOf("-c", configurationPath)
        val expected = listOf(
            Node.Link(
                getPath(temporaryFolder, "archlinux", "folder", "subfolder"),
                getPath(temporaryFolder, "pacman", "folder", "subfolder")
            )
        )

        main(args)

        val actual = collectLinkedFiles()
        assertEquals(expected, actual)
    }

    @Test
    fun `main when link map match both parent and child`() {
        val file = temporaryFolder.newFile(configurationBasename)
        file.writeText(
            """
            {
                "source": "${temporaryFolder.root.path}/pacman",
                "targets": [
                    "${temporaryFolder.root.path}/archlinux"
                ],
                "excludes": [],
                "linkMaps": [
                    {
                        "regex": "name",
                        "target": "${temporaryFolder.root.path}/archlinux"
                    }
                ]
            }
            """.trimIndent()
        )
        createNewFolder(temporaryFolder, "pacman/name/name")
        createNewFolder(temporaryFolder, "archlinux")
        val args = arrayOf("-c", configurationPath)
        val expected = listOf(
            Node.Link(
                getPath(temporaryFolder, "archlinux", "name"),
                getPath(temporaryFolder, "pacman", "name")
            )
        )

        main(args)

        val actual = collectLinkedFiles()
        assertEquals(expected, actual)
    }

    @Test
    fun `main when link source do not exists`() {
        val file = temporaryFolder.newFile(configurationBasename)
        file.writeText(
            """
            {
                "source": "${temporaryFolder.root.path}/pacman",
                "targets": [
                    "${temporaryFolder.root.path}/archlinux"
                ],
                "excludes": [],
                "linkMaps": []
            }
            """.trimIndent()
        )
        createNewFolder(temporaryFolder, "archlinux")
        createSymbolicLink(
            Node.Link(
                getPath(temporaryFolder, "archlinux", "name"),
                getPath(temporaryFolder, "pacman", "name")
            )
        )
        val args = arrayOf("-c", configurationPath)
        val expected = emptyList<Node.Link>()

        main(args)

        val actual = collectLinkedFiles()
        assertEquals(expected, actual)
    }
}
