package me.raatiniemi.linker.filter;

import java.util.List;
import java.util.Optional;

public class ExcludeFilter {
    /**
     * Filter item if found within the data.
     *
     * @param item Item to find in data.
     * @param data Data source.
     * @return false if item is found within data, otherwise true.
     */
    public static <T> boolean filter(T item, List<T> data) {
        Optional<T> found = data.stream()
                .filter(item::equals)
                .findFirst();

        return !found.isPresent();
    }
}
