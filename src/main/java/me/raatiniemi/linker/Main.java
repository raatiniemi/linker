package me.raatiniemi.linker;

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
        // Check that we have been supplied with a configuration file.
        if (0 == args.length || args[0].isEmpty()) {
            throw new RuntimeException(
                    "No configuration file have been supplied"
            );
        }

        // Check that the configuration file exists.
        Path configuration = Paths.get(args[0]);
        if (!Files.exists(configuration)) {
            throw new RuntimeException(
                    "Configuration file do not exist"
            );
        }

        // Load the configuration file into properties.
        Properties properties = new Properties();
        properties.load(Files.newInputStream(configuration));

        // Attempt to read the source directory.
        String sourceDirectory = properties.getProperty("source.directory");
        if (sourceDirectory.isEmpty()) {
            throw new RuntimeException(
                    "No source directory have been supplied"
            );
        }

        // Attempt to read the target directories.
        //
        // Since we need to support multiple target directories the target
        // directory properties have to be prefixed with 'target.directory',
        // and preferable suffixed with a numeric value.
        List<String> targetDirectories = properties.stringPropertyNames()
                .stream()
                .filter(key -> key.startsWith("target.directory"))
                .map(properties::getProperty)
                .collect(Collectors.toList());
        if (targetDirectories.isEmpty()) {
            throw new RuntimeException(
                    "No target directories have been supplied"
            );
        }

        // We have to walk through each of the target directories to find the
        // source directory of the symbolic links.
        //
        // TODO: Handle the null/empty values better.
        List<Item> targets = targetDirectories.stream()
                .map(Paths::get)
                .flatMap(directory -> {
                    try {
                        return Files.walk(directory);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(path -> null != path)
                .filter(Files::isSymbolicLink)
                .map(link -> {
                    try {
                        return Files.readSymbolicLink(link);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(path -> null != path)
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
        Path source = Paths.get(sourceDirectory);
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

        // List the sources and exclude the items existing within any of the
        // target directories.
        List<Directory> sources = directories.stream()
                .filter(directory -> {
                    Optional<Item> found = targets.stream()
                            .filter(target -> target.getBasename().equals(directory.getBasename()))
                            .findFirst();

                    return !found.isPresent();
                })
                .collect(Collectors.toList());

        // Print the number of targets and unlinked sources.
        System.out.println("Targets: " + targets.size());
        System.out.println("Sources: " + sources.size());

        // Print the unlinked sources.
        sources.stream()
                .map(Directory::getPath)
                .map(Path::getFileName)
                .sorted()
                .forEach(System.out::println);
    }
}
