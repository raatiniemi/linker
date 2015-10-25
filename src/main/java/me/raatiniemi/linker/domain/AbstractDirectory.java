package me.raatiniemi.linker.domain;

import me.raatiniemi.linker.configuration.LinkMap;
import me.raatiniemi.linker.filter.ExcludeFilter;
import me.raatiniemi.linker.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class AbstractDirectory implements Directory {
    private Path path;

    public AbstractDirectory(Path path) {
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
        return ExcludeFilter.filter(this, data);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean link(List<LinkMap> linkMaps) {
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
        // Since we are doing comparison based on the basename it doesn't
        // really matter whether the object is a Group or an Item.
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
    public String toString() {
        return this.getBasename();
    }
}
