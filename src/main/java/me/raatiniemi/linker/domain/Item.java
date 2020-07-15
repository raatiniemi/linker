/*
 * linker
 * Copyright (C) 2020 raatiniemi
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

import me.raatiniemi.linker.filter.ExcludeFilterKt;
import me.raatiniemi.linker.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Item implements Directory {
    private Path path;

    public Item(Path path) {
        this.path = path;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    @Override
    public String getBasename() {
        return this.getPath()
                .getFileName()
                .toString();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean filter(List<Directory> data) {
        return ExcludeFilterKt.excludeFilter(this, data);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean link(Set<LinkMap> linkMaps) {
        // Attempt to find a link map configuration based on the basename.
        Optional<LinkMap> linkMap = linkMaps.stream()
                .filter(map -> map.match(this.getBasename()))
                .findFirst();

        // If we were unable to find a configuration, i.e. we are unable to
        // link the item we have to return false.
        if (!linkMap.isPresent()) {
            return false;
        }

        LinkMap map = linkMap.get();

        // Build the path for the link and target.
        Path link = Paths.get(map.getTarget(), this.getBasename());
        Path target = Paths.get(map.getPrefix(), this.getBasename());

        // If the symbolic link is created we have to exclude the item from the
        // filter by returning false.
        return FileUtil.createSymbolicLink(link, target);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        // Since we are doing comparison based on the basename it doesn't
        // really matter whether the object is a CollectionItem or an Item.
        if (!(o instanceof Directory)) {
            return false;
        }

        // Do basic comparison based on the directory basename.
        //
        // TODO: Use absolute path instead?
        // If the directory names aren't unique comparing only the basename
        // will give false positives.
        Directory directory = (Directory) o;
        return this.getBasename()
                .equals(directory.getBasename());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Objects.hashCode(getBasename());

        return result;
    }

    @Override
    public String toString() {
        return this.getBasename();
    }
}
