package me.raatiniemi.linker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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
        List<Path> targets = targetDirectories.stream()
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
                .map(Path::getFileName)
                .collect(Collectors.toList());

        // List the sources and exclude the items existing within any of the
        // target directories.
        List<Path> sources = Files.list(Paths.get(sourceDirectory))
                .filter(Files::isDirectory)
                .map(Path::getFileName)
                .filter(source -> {
                    Optional<Path> found = targets.stream()
                            .filter(source::equals)
                            .findFirst();

                    return !found.isPresent();
                })
                .collect(Collectors.toList());

        // Print the number of targets and unlinked sources.
        System.out.println("Targets: " + targets.size());
        System.out.println("Sources: " + sources.size());

        // Print the unlinked sources.
        sources.stream()
                .map(Path::toString)
                .sorted()
                .forEach(System.out::println);
    }
}
