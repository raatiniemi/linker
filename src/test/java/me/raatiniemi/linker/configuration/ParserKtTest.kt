package me.raatiniemi.linker.configuration

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
                LinkMapConfiguration(
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
