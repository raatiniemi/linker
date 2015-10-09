package me.raatiniemi.linker.domain;

import me.raatiniemi.linker.filter.ExcludeFilter;

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

    /**
     * Setter method for items contained within the group.
     *
     * @param items Items contained within the group.
     */
    public void setItems(List<Item> items) {
        this.items = items;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean filter(List<Directory> targets) {
        // Check whether the group have been linked. If the group is linked
        // there is no need to check the subdirectories.
        if (!super.filter(targets)) {
            return false;
        }

        // The group have not been linked, we have to check if the contained
        // items have been linked.
        List<Item> items = this.getItems()
                .stream()
                .filter(item -> ExcludeFilter.filter(item, targets))
                .collect(Collectors.toList());

        // If the containing items have been linked we can filter the group.
        //
        // We need to update the items to only list unlinked items.
        this.setItems(items);
        return !this.getItems().isEmpty();
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
