package me.raatiniemi.linker.domain

internal interface LinkMap {
    val prefix: String
    val target: String

    /**
     * Check if the text matches the regex.
     *
     * @param text Text to check against regex.
     * @return true if text matches, otherwise false.
     */
    fun match(text: String): Boolean
}
