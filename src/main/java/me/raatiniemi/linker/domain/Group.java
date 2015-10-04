package me.raatiniemi.linker.domain;

import java.nio.file.Path;
import java.util.List;

public class Group extends AbstractDirectory {
    /**
     * Items contained within the group.
     */
    private List<Item> items;

    /**
     * Constructor.
     *
     * @param path Path for the group.
     * @param items Items contained within the group.
     */
    public Group(Path path, List<Item> items) {
        super(path);

        this.items = items;
    }

    /**
     * Getter method for items contained within the group.
     *
     * @return Items contained within the group.
     */
    public List<Item> getItems() {
        return this.items;
    }
}
