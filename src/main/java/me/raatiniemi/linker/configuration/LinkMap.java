package me.raatiniemi.linker.configuration;

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
}
