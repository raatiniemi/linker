package me.raatiniemi.linker;

import java.util.List;

public class Configuration {
    private String source;

    private List<String> targetDirectories;

    private List<String> excludeDirectories;

    public String getSource() {
        return source;
    }

    @SuppressWarnings("unused")
    public void setSource(String source) {
        this.source = source;
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
