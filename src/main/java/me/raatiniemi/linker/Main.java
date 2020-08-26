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
import me.raatiniemi.linker.domain.CollectionItem;
import me.raatiniemi.linker.domain.Directory;
import me.raatiniemi.linker.domain.Item;
import me.raatiniemi.linker.domain.LinkMap;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static me.raatiniemi.linker.CollectTargetNodesKt.collectTargetNodes;
import static me.raatiniemi.linker.ConfigureExcludeDirectoriesKt.configureExcludeDirectories;
import static me.raatiniemi.linker.configuration.ParserKt.parseConfiguration;

public class Main {
    public static void main(String... args) throws IOException {
        Configuration configuration = parseConfigurationFileFromArguments(args);

        List<String> excludeDirectories = configureExcludeDirectories(configuration);
        List<Directory> targetNodes = collectTargetNodes(configuration.getTargets());
        List<Directory> sourceNodes = collectSourceNodes(configuration, excludeDirectories);
        List<Directory> sources = linkSourceNodesIntoTargets(configuration, targetNodes, sourceNodes);

        printReportForCollectionSizes(targetNodes, sources);
        printReportForUnlinkedNodes(sources);
    }

    private static Configuration parseConfigurationFileFromArguments(String[] args) {
        if (0 == args.length || args[0].isEmpty()) {
            throw new RuntimeException("No configuration file have been supplied");
        }

        return parseConfiguration(args[0]);
    }

    @NotNull
    private static List<Directory> collectSourceNodes(
            @NotNull Configuration configuration,
            @NotNull List<String> excludeDirectories
    ) throws IOException {
        Map<Path, List<Item>> rawSourceNodes = collectRawSourceNodes(
                Paths.get(configuration.getSource()),
                excludeDirectories
        );

        // Build the mapped structure from the raw data. Depending on whether
        // the item have children the item should be mapped as Item or CollectionItem.
        //
        // Directory 1 (Item)
        // Directory 2 (CollectionItem)
        //    Directory 3 (Item)
        //    Directory 4 (Item)
        List<Directory> directories = new ArrayList<>();
        rawSourceNodes.forEach((path, children) -> {
            if (children.isEmpty()) {
                directories.add(new Item(path));
                return;
            }

            directories.add(new CollectionItem(path, children));
        });

        return directories;
    }

    /**
     * Store the mapped raw data from the source directory.
     * <p>
     * We have to map the raw data before attempting to filter anything,
     * otherwise we can't handle grouped directories.
     * <p>
     * The data is mapped as follows:
     * <p>
     * Directory 1 -> []
     * Directory 2 -> [Directory 3, Directory 4]
     * <p>
     * Depending on if the value is empty at the end of the walk determines
     * whether the directory is a group or single item.
     */
    @NotNull
    static Map<Path, List<Item>> collectRawSourceNodes(
            Path source,
            @NotNull List<String> excludeDirectories
    ) throws IOException {
        Map<Path, List<Item>> rawMap = new HashMap<>();
        Files.walk(source, 2)
                .filter(Files::isDirectory)
                .filter(path -> {
                    // When walking with `Files.walk` the source directory is
                    // listed along with the other directories.
                    //
                    // We have to filter away the source directory since we
                    // only want to map two levels.
                    try {
                        return !Files.isSameFile(path, source);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .filter(path -> {
                    // Check if the name of the directory is included within
                    // the exclude directories.
                    String filename = path.getFileName()
                            .toString()
                            .toLowerCase();

                    return !excludeDirectories.contains(filename);
                })
                .sorted()
                .forEach(path -> {
                    // Since we are sorting the stream before building the raw
                    // data, we can safely assume that parent directories will
                    // appear before its children.
                    //
                    // Directory 1
                    // Directory 2/Directory 3
                    // Directory 2/Directory 4
                    //
                    // If the parent do not exists within the data, we have to
                    // initialize it with an empty array.
                    //
                    // And, then just add the children.
                    if (!rawMap.containsKey(path.getParent())) {
                        rawMap.put(path, new ArrayList<>());
                        return;
                    }

                    rawMap.get(path.getParent())
                            .add(new Item(path));
                });

        return rawMap;
    }

    @NotNull
    private static List<Directory> linkSourceNodesIntoTargets(
            @NotNull Configuration configuration,
            @NotNull List<Directory> targetNodes,
            @NotNull List<Directory> sourceNodes
    ) {
        Set<LinkMap> linkMaps = configuration.getLinkMaps();

        // List the sources and exclude the items existing within any of the
        // target directories.
        return sourceNodes.stream()
                .filter(directory -> directory.filter(targetNodes))
                .filter(directory ->
                        // Since we want to exclude items that are considered
                        // linked we have to inverse the return value.
                        !directory.link(linkMaps))
                .collect(Collectors.toList());
    }

    private static void printReportForCollectionSizes(@NotNull List<Directory> targetNodes, @NotNull List<Directory> sources) {
        // Print the number of targets and unlinked sources.
        System.out.println("Targets: " + targetNodes.size());
        System.out.println("Sources: " + sources.size());
    }

    private static void printReportForUnlinkedNodes(@NotNull List<Directory> sources) {
        // Print the unlinked sources.
        sources.stream()
                .sorted(Comparator.comparing(Directory::getBasename))
                .forEach(System.out::println);
    }
}
