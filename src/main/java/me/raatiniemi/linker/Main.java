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

package me.raatiniemi.linker;

import me.raatiniemi.linker.configuration.Configuration;
import me.raatiniemi.linker.configuration.ConfigurationParser;
import me.raatiniemi.linker.domain.LinkMap;
import me.raatiniemi.linker.domain.Directory;
import me.raatiniemi.linker.domain.Group;
import me.raatiniemi.linker.domain.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String... args) throws IOException {
        Configuration configuration = parseConfigurationFileFromArguments(args);

        // The comparison have to be case insensitive, so everything have to
        // be converted to lowercase.
        List<String> excludeDirectories = configuration.getExcludes()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        // We have to walk through each of the target directories to find the
        // source directory of the symbolic links.
        //
        // TODO: Handle the null/empty values better.
        List<Directory> targets = configuration.getTargets().stream()
                .map(Paths::get)
                .flatMap(directory -> {
                    try {
                        return Files.walk(directory);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(Files::isSymbolicLink)
                .map(link -> {
                    try {
                        return Files.readSymbolicLink(link);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(Item::new)
                .collect(Collectors.toList());

        // Store the mapped raw data from the source directory.
        //
        // We have to map the raw data before attempting to filter anything,
        // otherwise we can't handle grouped directories.
        //
        // The data is mapped as follows:
        //
        // Directory 1 -> []
        // Directory 2 -> [Directory 3, Directory 4]
        //
        // Depending on if the value is empty at the end of the walk determinds
        // whether the directory is a group or single item.
        Map<Path, List<Item>> rawMap = new HashMap<>();

        // TODO: Add support for recursive mapping.
        Path source = Paths.get(configuration.getSource());
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

        // Build the mapped structure from the raw data. Depending on whether
        // the item have children the item should be mapped as Item or Group.
        //
        // Directory 1 (Item)
        // Directory 2 (Group)
        //    Directory 3 (Item)
        //    Directory 4 (Item)
        List<Directory> directories = new ArrayList<>();
        rawMap.forEach((path, children) -> {
            if (children.isEmpty()) {
                directories.add(new Item(path));
                return;
            }

            directories.add(new Group(path, children));
        });

        Set<LinkMap> linkMaps = configuration.getLinkMaps();

        // List the sources and exclude the items existing within any of the
        // target directories.
        List<Directory> sources = directories.stream()
                .filter(directory -> directory.filter(targets))
                .filter(directory ->
                        // Since we want to exclude items that are considered
                        // linked we have to inverse the return value.
                        !directory.link(linkMaps))
                .collect(Collectors.toList());

        // Print the number of targets and unlinked sources.
        System.out.println("Targets: " + targets.size());
        System.out.println("Sources: " + sources.size());

        // Print the unlinked sources.
        sources.stream()
                .sorted(Comparator.comparing(Directory::getBasename))
                .forEach(System.out::println);
    }

    private static Configuration parseConfigurationFileFromArguments(String[] args) {
        if (0 == args.length || args[0].isEmpty()) {
            throw new RuntimeException("No configuration file have been supplied");
        }

        return ConfigurationParser.parse(args[0]);
    }
}
