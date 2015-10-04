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
}
