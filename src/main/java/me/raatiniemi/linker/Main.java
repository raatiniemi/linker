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

package me.raatiniemi.linker;

import me.raatiniemi.linker.configuration.Configuration;
import me.raatiniemi.linker.domain.LinkMap;
import me.raatiniemi.linker.domain.Node;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.raatiniemi.linker.CollectSourceNodesKt.collectSourceNodes;
import static me.raatiniemi.linker.CollectTargetNodesKt.collectTargetNodes;
import static me.raatiniemi.linker.ConfigureExcludeDirectoriesKt.configureExcludeDirectories;
import static me.raatiniemi.linker.configuration.ParserKt.parseConfiguration;
import static me.raatiniemi.linker.domain.NodesKt.filter;
import static me.raatiniemi.linker.domain.NodesKt.print;

public class Main {
    public static void main(String... args) {
        Configuration configuration = parseConfigurationFileFromArguments(args);

        List<String> excludeDirectories = configureExcludeDirectories(configuration);
        List<Node.Link> targetNodes = collectTargetNodes(configuration.getTargets());
        List<Node> sourceNodes = collectSourceNodes(Paths.get(configuration.getSource()), excludeDirectories);
        List<Node> sources = linkSourceNodesIntoTargets(configuration, targetNodes, sourceNodes);

        printReportForCollectionSizes(targetNodes, sources);
        print(sources);
    }

    private static Configuration parseConfigurationFileFromArguments(String[] args) {
        if (0 == args.length || args[0].isEmpty()) {
            throw new RuntimeException("No configuration file have been supplied");
        }

        return parseConfiguration(args[0]);
    }

    @NotNull
    private static List<Node> linkSourceNodesIntoTargets(
            @NotNull Configuration configuration,
            @NotNull List<Node.Link> targetNodes,
            @NotNull List<Node> sourceNodes
    ) {
        Set<LinkMap> linkMaps = configuration.getLinkMaps();

        // List the sources and exclude the items existing within any of the
        // target directories.
        return filter(sourceNodes, targetNodes)
                .stream()
                .filter(directory ->
                        // Since we want to exclude items that are considered
                        // linked we have to inverse the return value.
                        !directory.link(linkMaps))
                .collect(Collectors.toList());
    }

    private static void printReportForCollectionSizes(@NotNull List<Node.Link> targetNodes, @NotNull List<Node> sources) {
        // Print the number of targets and unlinked sources.
        System.out.println("Targets: " + targetNodes.size());
        System.out.println("Sources: " + sources.size());
    }
}
