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
package me.raatiniemi.linker.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import me.raatiniemi.linker.domain.LinkMap
import java.util.*
import java.util.regex.Pattern

internal data class LinkMapConfiguration(
    @JsonProperty("regex")
    private val regex: String,
    @JsonProperty("prefix")
    override val prefix: String,
    @JsonProperty("target")
    override val target: String
) : LinkMap {
    override fun match(text: String): Boolean {
        return (Objects.nonNull(regex)
                && regex.isNotEmpty()
                && Pattern.matches(regex, text))
    }
}