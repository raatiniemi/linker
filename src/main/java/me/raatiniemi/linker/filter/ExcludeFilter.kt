/*
 * Copyright (C) 2015 Raatiniemi
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

/**
 * Filter item if found within the data.
 *
 * @param item Item to find in data.
 * @param data Data source.
 * @return false if item is found within data, otherwise true.
 */
internal fun <T> excludeFilter(item: T, data: List<T>): Boolean {
    return data.firstOrNull { item == it } == null
}
