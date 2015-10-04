package me.raatiniemi.linker.domain;

import java.nio.file.Path;

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
}
