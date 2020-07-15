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

import com.fasterxml.jackson.annotation.JsonProperty
import me.raatiniemi.linker.domain.LinkMap

/**
 * Represent the configuration file.
 */
internal data class Configuration(
    @JsonProperty("source")
    val source: String,
    @JsonProperty("targets")
    val targets: List<String>,
    @JsonProperty("excludes")
    val excludes: List<String> = emptyList(),
    @JsonProperty("linkMaps")
    val linkMaps: Set<LinkMap> = emptySet()
)
