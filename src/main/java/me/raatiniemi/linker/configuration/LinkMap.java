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

import java.util.Objects;
import java.util.regex.Pattern;

public class LinkMap {
    private String regex;

    private String prefix;

    private String target;

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Check if the text matches the regex.
     *
     * @param text Text to check against regex.
     * @return true if text matches, otherwise false.
     */
    public boolean match(String text) {
        return null != this.getRegex()
                && !this.getRegex().isEmpty()
                && Pattern.matches(this.getRegex(), text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LinkMap)) {
            return false;
        }

        LinkMap linkMap = (LinkMap) o;
        return Objects.equals(regex, linkMap.regex)
                && Objects.equals(prefix, linkMap.prefix)
                && Objects.equals(target, linkMap.target);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Objects.hashCode(regex);
        result = 31 * result + Objects.hashCode(prefix);
        result = 31 * result + Objects.hashCode(target);

        return result;
    }
}
