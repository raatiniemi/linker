package me.raatiniemi.linker.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;

public final class ConfigurationParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(ALLOW_COMMENTS, true);
    }

    public static Configuration parse(String filename) {
        return parse(Paths.get(filename));
    }

    private static Configuration parse(Path file) {
        if (!Files.exists(file)) {
            throw new RuntimeException("Configuration file do not exist");
        }

        try {
            return mapper.readValue(Files.newInputStream(file), Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read configuration file", e);
        }
    }
}
