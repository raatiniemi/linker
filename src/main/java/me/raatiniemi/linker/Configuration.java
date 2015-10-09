package me.raatiniemi.linker;

import java.util.List;

public class Configuration {
    private String sourceDirectory;

    private List<String> targetDirectories;

    private List<String> excludeDirectories;

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    @SuppressWarnings("unused")
    public void setSourceDirectory(String directory) {
        this.sourceDirectory = directory;
    }

    public List<String> getTargetDirectories() {
        return targetDirectories;
    }

    @SuppressWarnings("unused")
    public void setTargetDirectories(List<String> targetDirectories) {
        this.targetDirectories = targetDirectories;
    }

    public List<String> getExcludeDirectories() {
        return excludeDirectories;
    }

    @SuppressWarnings("unused")
    public void setExcludeDirectories(List<String> excludeDirectories) {
        this.excludeDirectories = excludeDirectories;
    }
}
