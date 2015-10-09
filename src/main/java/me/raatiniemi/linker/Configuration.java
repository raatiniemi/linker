package me.raatiniemi.linker;

import java.util.List;

public class Configuration {
    private String source;

    private List<String> targets;

    private List<String> excludeDirectories;

    public String getSource() {
        return source;
    }

    @SuppressWarnings("unused")
    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getTargets() {
        return targets;
    }

    @SuppressWarnings("unused")
    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    public List<String> getExcludeDirectories() {
        return excludeDirectories;
    }

    @SuppressWarnings("unused")
    public void setExcludeDirectories(List<String> excludeDirectories) {
        this.excludeDirectories = excludeDirectories;
    }
}
