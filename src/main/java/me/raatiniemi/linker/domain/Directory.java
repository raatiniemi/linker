package me.raatiniemi.linker.domain;

import me.raatiniemi.linker.configuration.LinkMap;

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

    /**
     * Attempt to link directory if link map configuration is found.
     *
     * @param linkMaps Link map configurations.
     * @return true if item was linked, otherwise false.
     */
    boolean link(List<LinkMap> linkMaps);
}
