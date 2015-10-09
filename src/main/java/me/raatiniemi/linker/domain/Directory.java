package me.raatiniemi.linker.domain;

import java.nio.file.Path;
import java.util.List;

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

    /**
     * Filter item based on the data.
     *
     * @param data Data source.
     * @return false if item is found within data, otherwise true.
     */
    boolean filter(List<Directory> data);
}
