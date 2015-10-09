package me.raatiniemi.linker;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.raatiniemi.linker.domain.Directory;
import me.raatiniemi.linker.domain.Group;
import me.raatiniemi.linker.domain.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;

public class Main {
    public static void main(String... args) throws IOException {
        // Check that we have been supplied with a configuration file.
        if (0 == args.length || args[0].isEmpty()) {
            throw new RuntimeException(
                    "No configuration file have been supplied"
            );
        }

        // Check that the configuration file exists.
        Path file = Paths.get(args[0]);
        if (!Files.exists(file)) {
            throw new RuntimeException(
                    "Configuration file do not exist"
            );
        }

        // Setup the JSON to Object mapper.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ALLOW_COMMENTS, true);

        // Attempt to read the JSON configuration.
        Configuration configuration = objectMapper.readValue(
                Files.newInputStream(file),
                Configuration.class
        );

        // Check that a source directory have been
        // supplied via the configuration file.
        String sourceDirectory = configuration.getSource();
        if (null == sourceDirectory || sourceDirectory.isEmpty()) {
            throw new RuntimeException(
                    "No source directory have been supplied"
            );
        }

        // Check that a target directories have been
        // supplied via the configuration file.
        List<String> targetDirectories = configuration.getTargets();
        if (null == targetDirectories || targetDirectories.isEmpty()) {
            throw new RuntimeException(
                    "No target directories have been supplied"
            );
        }

        // Check whether we have directories to exclude.
        if (null == configuration.getExcludes()) {
            configuration.setExcludes(new ArrayList<>());
        }

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
        List<Directory> targets = targetDirectories.stream()
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

        // List the sources and exclude the items existing within any of the
        // target directories.
        List<Directory> sources = directories.stream()
                .filter(directory -> {
                    // TODO: Migrate filter to objects.
                    // TODO: Check if item is linked before checking content.
                    if (directory instanceof Group) {
                        // If we're working with a group we have to check if
                        // the contained items have been linked.
                        Group group = (Group) directory;
                        List<Item> items = group.getItems()
                                .stream()
                                .filter(item -> {
                                    Optional<Directory> found = targets.stream()
                                            .filter(item::equals)
                                            .findFirst();

                                    return !found.isPresent();
                                })
                                .collect(Collectors.toList());

                        // If all items within the gorup have been linked we
                        // should exclude the group.
                        //
                        // We need to update items to only list unlinked.
                        group.setItems(items);
                        return !items.isEmpty();
                    }

                    Optional<Directory> found = targets.stream()
                            .filter(directory::equals)
                            .findFirst();

                    return !found.isPresent();
                })
                .collect(Collectors.toList());

        // Print the number of targets and unlinked sources.
        System.out.println("Targets: " + targets.size());
        System.out.println("Sources: " + sources.size());

        // Print the unlinked sources.
        sources.stream()
                .sorted((d1, d2) -> d1.getBasename().compareTo(d2.getBasename()))
                .forEach(System.out::println);
    }
}
