/*
 * Copyright (C) 2015 Raatiniemi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.raatiniemi.linker.domain;

import me.raatiniemi.linker.filter.ExcludeFilter;
import me.raatiniemi.linker.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Group extends Item {
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

    /**
     * @inheritDoc
     */
    @Override
    public boolean link(Set<LinkMap> linkMaps) {
        List<Item> items = this.getItems().stream()
                .filter(item -> {
                    Optional<LinkMap> linkMap = linkMaps.stream()
                            .filter(map -> map.match(item.getBasename()))
                            .findFirst();

                    // If we were unable to find a configuration, i.e. we are
                    // unable to link the item we have to return false.
                    if (!linkMap.isPresent()) {
                        return true;
                    }

                    LinkMap map = linkMap.get();

                    // Build the path for the link and target.
                    Path link = Paths.get(map.getTarget(), item.getBasename());
                    Path target = Paths.get(map.getPrefix(), this.getBasename(), item.getBasename());

                    // If the symbolic link is created we have to exclude the
                    // item from the filter by returning false.
                    return !FileUtil.createSymbolicLink(link, target);
                })
                .collect(Collectors.toList());

        // If the containing items have been linked we can filter the group.
        //
        // We need to update the items to only list unlinked items.
        this.setItems(items);
        return this.getItems().isEmpty();
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
