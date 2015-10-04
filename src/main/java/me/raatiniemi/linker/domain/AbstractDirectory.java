package me.raatiniemi.linker.domain;

import java.nio.file.Path;

public class AbstractDirectory implements Directory {
    private Path path;

    public AbstractDirectory(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return this.path;
    }
}
