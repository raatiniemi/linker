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

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class LinkMapConfigurationTest(
    private val expected: Boolean,
    private val regex: String,
    private val match: String
) {
    @Test
    fun match() {
        val linkMap = LinkMapConfiguration(regex, "", "")

        assertEquals(expected, linkMap.match(match))
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        @get:Parameters
        val parameters: Collection<Array<Any>>
            get() = listOf(
                arrayOf(
                    false,
                    "",
                    "without-regex"
                ),
                arrayOf(
                    false,
                    "match",
                    "without-match"
                ),
                arrayOf(
                    false,
                    "^match[-]\\d+$",
                    "without-match"
                ),
                arrayOf(
                    false,
                    "^(?i)match[-]\\d+$",
                    "without-match"
                ),
                arrayOf(
                    true,
                    "match",
                    "match"
                ),
                arrayOf(
                    true,
                    "^match[-]\\d+$",
                    "match-20"
                ),
                arrayOf(
                    true,
                    "^(?i)match[-]\\d+$",
                    "Match-530"
                )
            )
    }
}
