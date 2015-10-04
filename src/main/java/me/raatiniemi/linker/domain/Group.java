package me.raatiniemi.linker.domain;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        String value = super.toString();

        // If the group contain any items they should be appended to the value.
        //
        // Directory 1
        // Directory 2 (Group)
        //     Directory 3
        //     Directory 4
        List<String> items = this.getItems()
                .stream()
                .map(Directory::getBasename)
                .collect(Collectors.toList());
        if (!items.isEmpty()) {
            String separator = "\n  ";
            value += separator + String.join(separator, items);
        }

        return value;
    }
}
