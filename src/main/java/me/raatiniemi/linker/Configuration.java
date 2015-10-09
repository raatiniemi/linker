package me.raatiniemi.linker;

import java.util.List;

public class Configuration {
    private String source;

    private List<String> targets;

    private List<String> excludes;

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

    public List<String> getExcludes() {
        return excludes;
    }

    @SuppressWarnings("unused")
    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }
}
