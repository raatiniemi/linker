package me.raatiniemi.linker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
    }
}
