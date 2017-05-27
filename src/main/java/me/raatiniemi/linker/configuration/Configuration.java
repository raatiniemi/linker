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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.raatiniemi.linker.domain.LinkMap;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * Represent the configuration file.
 */
public class Configuration {
    private final String source;
    private final List<String> targets;
    private final List<String> excludes;
    private final Set<LinkMap> linkMaps;

    @JsonCreator
    public Configuration(
            @JsonProperty("source") String source,
            @JsonProperty("targets") List<String> targets,
            @JsonProperty("excludes") List<String> excludes,
            @JsonProperty("linkMaps") Set<LinkMap> linkMaps
    ) {
        this.source = source;
        this.targets = targets;

        if (isNull(excludes)) {
            excludes = Collections.emptyList();
        }
        this.excludes = excludes;

        if (isNull(linkMaps)) {
            linkMaps = Collections.emptySet();
        }
        this.linkMaps = linkMaps;
    }

    /**
     * Getter method for source directory.
     *
     * @return Path to source directory.
     */
    public String getSource() {
        return source;
    }

    /**
     * Getter method for target directories.
     *
     * @return Path to target directories.
     */
    public List<String> getTargets() {
        return Collections.unmodifiableList(targets);
    }

    /**
     * Getter method for exclude directories.
     *
     * @return Basename of exclude directories.
     */
    public List<String> getExcludes() {
        return Collections.unmodifiableList(excludes);
    }

    /**
     * Getter method for link map configurations.
     *
     * @return Link map configurations.
     */
    public Set<LinkMap> getLinkMaps() {
        return Collections.unmodifiableSet(linkMaps);
    }
}
