package me.raatiniemi.linker.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    /**
     * Create a symbolic link between two locations.
     *
     * @param link Location of the link.
     * @param target Target of the link.
     * @return true if symbolic link was created, otherwise false.
     */
    public static boolean createSymbolicLink(Path link, Path target) {
        boolean linked = false;

        System.out.println("Linking: " + link.getFileName());
        try {
            Files.createSymbolicLink(link, target);
            linked = true;
        } catch (IOException e) {
            System.out.println("Failed to link: " + e.getMessage());
        }

        return linked;
    }
}
