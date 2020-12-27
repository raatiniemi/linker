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

internal sealed class ConfigurationException(message: String, e: Throwable? = null) : RuntimeException(message, e)

internal class UnableToFindConfigurationFile(message: String) : ConfigurationException(message)

internal class InvalidConfigurationFile(message: String, e: Throwable? = null) : ConfigurationException(message, e)

