package me.raatiniemi.linker.domain;

public interface LinkMap {
    String getPrefix();

    String getTarget();

    /**
     * Check if the text matches the regex.
     *
     * @param text Text to check against regex.
     * @return true if text matches, otherwise false.
     */
    boolean match(String text);
}
