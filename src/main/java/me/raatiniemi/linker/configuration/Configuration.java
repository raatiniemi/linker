/*
 * Copyright (C) 2015 Raatiniemi
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

package me.raatiniemi.linker.configuration;

import me.raatiniemi.linker.domain.LinkMap;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    private List<String> excludes = Collections.emptyList();

    /**
     * Link map configurations.
     */
    private Set<LinkMap> linkMaps = Collections.emptySet();

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
    public Set<LinkMap> getLinkMaps() {
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
    public void setLinkMaps(Set<LinkMap> linkMaps) {
        this.linkMaps = linkMaps;
    }
}
