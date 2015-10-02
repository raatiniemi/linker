package me.raatiniemi.linker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String... args) {
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
    }
}
