package me.raatiniemi.linker.domain;

import java.nio.file.Path;

public interface Directory {
    /**
     * Getter method for the path of the directory.
     *
     * @return Path of the directory.
     */
    Path getPath();

    /**
     * Getter method for the basename of the directory.
     *
     * @return Basename for the directory.
     */
    String getBasename();
}
