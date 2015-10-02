package me.raatiniemi.linker;

public class Main {
    public static void main(String... args) {
        // Check that we have been supplied with a configuration file.
        if (0 == args.length || args[0].isEmpty()) {
            throw new RuntimeException(
                    "No configuration file have been supplied"
            );
        }
    }
}
