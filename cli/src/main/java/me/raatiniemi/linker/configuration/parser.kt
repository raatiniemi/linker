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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private val objectMapper: ObjectMapper by lazy {
    ObjectMapper()
        .also {
            it.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            it.registerModule(KotlinModule())
        }
}

internal fun parseConfiguration(filename: String): Configuration {
    return parse(Paths.get(filename))
}

private fun parse(file: Path): Configuration {
    if (!Files.exists(file)) {
        throw UnableToFindConfigurationFile("Configuration file do not exist")
    }
    val configuration = parseConfigurationFromFile(file)
    return validateConfiguration(configuration)
}

private fun parseConfigurationFromFile(file: Path): Configuration {
    return try {
        objectMapper.readValue(Files.newInputStream(file), Configuration::class.java)
    } catch (e: IOException) {
        throw InvalidConfigurationFile("Unable to read configuration file", e)
    }
}

private fun validateConfiguration(configuration: Configuration): Configuration {
    if (configuration.source.isEmpty()) {
        throw InvalidConfigurationFile("No source directory have been supplied")
    }

    if (configuration.targets.isEmpty()) {
        throw InvalidConfigurationFile("No target directories have been supplied")
    }

    return configuration
}
