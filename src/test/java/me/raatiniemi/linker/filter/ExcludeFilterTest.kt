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
package me.raatiniemi.linker.filter

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ExcludeFilterTest {
    @Test
    fun `filter without object`() {
        val object1 = Any()
        val object2 = Any()
        val data = listOf(
            object1
        )
        val expected = true

        val actual = excludeFilter(object2, data)

        assertEquals(expected, actual)
    }

    @Test
    fun `filter with object`() {
        val object1 = Any()
        val object2 = Any()
        val data = listOf(
            object1,
            object2
        )
        val expected = false

        val actual = excludeFilter(object2, data)

        assertEquals(expected, actual)
    }
}
