package me.raatiniemi.linker.configuration;

import java.util.List;

/**
 * Represent the configuration file.
 */
public class Configuration {
    /**
     * Path to source directory.
     */
    private String source;

    /**
     * Path to target directories.
     */
    private List<String> targets;

    /**
     * Basename of exclude directories.
     */
    private List<String> excludes;

    /**
     * Link map configurations.
     */
    private List<LinkMap> linkMaps;

    /**
     * Getter method for source directory.
     *
     * @return Path to source directory.
     */
    public String getSource() {
        return source;
    }

    /**
     * Setter method for source directory.
     *
     * Suppressing warnings for unused method because the method is called via
     * the ObjectMapper from the jackson library.
     *
     * @param source Path to source directory.
     */
    @SuppressWarnings("unused")
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Getter method for target directories.
     *
     * @return Path to target directories.
     */
    public List<String> getTargets() {
        return targets;
    }

    /**
     * Setter method for target directories.
     *
     * Suppressing warnings for unused method because the method is called via
     * the ObjectMapper from the jackson library.
     *
     * @param targets Path to target directories.
     */
    @SuppressWarnings("unused")
    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    /**
     * Getter method for exclude directories.
     *
     * @return Basename of exclude directories.
     */
    public List<String> getExcludes() {
        return excludes;
    }

    /**
     * Setter method for exclude directories.
     *
     * Suppressing warnings for unused method because the method is called via
     * the ObjectMapper from the jackson library.
     *
     * @param excludes Basename of exclude directories.
     */
    @SuppressWarnings("unused")
    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    /**
     * Getter method for link map configurations.
     *
     * @return Link map configurations.
     */
    public List<LinkMap> getLinkMaps() {
        return linkMaps;
    }

    /**
     * Setter method for link map configurations.
     *
     * Suppressing warnings for unused method because the method is called via
     * the ObjectMapper from the jackson-databind library.
     *
     * @param linkMaps Link map configurations.
     */
    @SuppressWarnings("unused")
    public void setLinkMaps(List<LinkMap> linkMaps) {
        this.linkMaps = linkMaps;
    }
}
