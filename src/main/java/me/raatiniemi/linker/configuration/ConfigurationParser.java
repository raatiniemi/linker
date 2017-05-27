package me.raatiniemi.linker.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static java.util.Objects.isNull;

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

        Configuration configuration = parseConfigurationFromFile(file);
        return validateConfiguration(configuration);
    }

    private static Configuration parseConfigurationFromFile(Path file) {
        try {
            return mapper.readValue(Files.newInputStream(file), Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read configuration file", e);
        }
    }

    private static Configuration validateConfiguration(Configuration configuration) {
        String source = configuration.getSource();
        if (isNull(source) || source.isEmpty()) {
            throw new RuntimeException("No source directory have been supplied");
        }

        List<String> targets = configuration.getTargets();
        if (isNull(targets) || targets.isEmpty()) {
            throw new RuntimeException("No target directories have been supplied");
        }

        return configuration;
    }
}
