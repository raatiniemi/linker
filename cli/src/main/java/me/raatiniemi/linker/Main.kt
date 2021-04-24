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

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import me.raatiniemi.linker.configuration.parseConfiguration
import me.raatiniemi.linker.domain.Node
import me.raatiniemi.linker.domain.dryRunLink
import me.raatiniemi.linker.domain.filter
import me.raatiniemi.linker.domain.link
import me.raatiniemi.linker.domain.print
import java.nio.file.Paths
import kotlin.io.println

fun main(args: Array<String>) {
    val arguments = parseArguments(args)
    val excludeDirectories = configureExcludeDirectories(arguments.configuration)
    val targetNodes = collectTargetNodes(arguments.configuration.targets)
    val sourceNodes = collectSourceNodes(Paths.get(arguments.configuration.source), excludeDirectories)
    val nodes = filter(sourceNodes, targetNodes)
    val sources = if (arguments.dryRun) {
        dryRunLink(nodes, arguments.configuration.linkMaps)
    } else {
        link(nodes, arguments.configuration.linkMaps)
    }

    printReportForCollectionSizes(targetNodes, sources)
    print(sources)
}

private fun parseArguments(args: Array<String>): Arguments {
    val parser = ArgParser("linker")
    val configurationPath by parser.option(
        type = ArgType.String,
        fullName = "configuration",
        shortName = "c",
        description = "Path to configuration file"
    )
    val dryRun by parser.option(
        type = ArgType.Boolean,
        fullName = "dry-run",
        description = "Run without performing any actual changes"
    )
    parser.parse(args)

    val filename = checkNotNull(configurationPath) {
        "No configuration path is available"
    }
    return Arguments(
        configuration = parseConfiguration(filename),
        dryRun = dryRun ?: false
    )
}

private fun printReportForCollectionSizes(targetNodes: List<Node.Link>, sources: List<Node>) {
    println("Targets: ${targetNodes.size}")
    println("Sources: ${sources.size}")
}
