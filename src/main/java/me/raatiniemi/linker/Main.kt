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

import me.raatiniemi.linker.configuration.Configuration
import me.raatiniemi.linker.configuration.parseConfiguration
import me.raatiniemi.linker.domain.Node
import me.raatiniemi.linker.domain.filter
import me.raatiniemi.linker.domain.link
import me.raatiniemi.linker.domain.print
import java.nio.file.Paths

fun main(args: Array<String>) {
    val configuration = parseConfigurationFileFromArguments(args)
    val excludeDirectories = configureExcludeDirectories(configuration)
    val targetNodes = collectTargetNodes(configuration.targets)
    val sourceNodes = collectSourceNodes(Paths.get(configuration.source), excludeDirectories)
    val sources = link(filter(sourceNodes, targetNodes), configuration.linkMaps)

    printReportForCollectionSizes(targetNodes, sources)
    print(sources)
}

private fun parseConfigurationFileFromArguments(args: Array<String>): Configuration {
    if (args.isEmpty() || args[0].isEmpty()) {
        throw RuntimeException("No configuration file have been supplied")
    }
    return parseConfiguration(args[0])
}

private fun printReportForCollectionSizes(targetNodes: List<Node.Link>, sources: List<Node>) {
    println("Targets: ${targetNodes.size}")
    println("Sources: ${sources.size}")
}
