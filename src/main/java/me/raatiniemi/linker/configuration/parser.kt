package me.raatiniemi.linker.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import me.raatiniemi.linker.domain.LinkMap
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private val objectMapper: ObjectMapper by lazy {
    ObjectMapper()
        .also {
            val resolver = SimpleAbstractTypeResolver()
            resolver.addMapping(LinkMap::class.java, LinkMapConfiguration::class.java)

            val module = SimpleModule("LinkerModule", Version.unknownVersion())
            module.setAbstractTypes(resolver)

            it.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            it.registerModule(module)
            it.registerModule(KotlinModule())
        }
}

internal fun parseConfiguration(filename: String): Configuration {
    return parse(Paths.get(filename))
}

private fun parse(file: Path): Configuration {
    if (!Files.exists(file)) {
        throw RuntimeException("Configuration file do not exist")
    }
    val configuration = parseConfigurationFromFile(file)
    return validateConfiguration(configuration)
}

private fun parseConfigurationFromFile(file: Path): Configuration {
    return try {
        objectMapper.readValue(Files.newInputStream(file), Configuration::class.java)
    } catch (e: IOException) {
        throw RuntimeException("Unable to read configuration file", e)
    }
}

private fun validateConfiguration(configuration: Configuration): Configuration {
    if (configuration.source.isEmpty()) {
        throw RuntimeException("No source directory have been supplied")
    }

    if (configuration.targets.isEmpty()) {
        throw RuntimeException("No target directories have been supplied")
    }

    return configuration
}
