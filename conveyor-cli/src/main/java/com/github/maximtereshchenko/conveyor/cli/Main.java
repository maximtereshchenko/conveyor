package com.github.maximtereshchenko.conveyor.cli;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.core.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

final class Main {

    private static final String CONFIG_FILE_PROPERTY = "java.util.logging.config.file";

    public static void main(String[] args) throws IOException {
        mergeLoggingConfiguration();
        new ConveyorFacade(JacksonAdapter.configured())
            .construct(
                Paths.get(args[0]).toAbsolutePath().normalize(),
                Arrays.stream(args, 1, args.length)
                    .map(String::toUpperCase)
                    .map(Stage::valueOf)
                    .toList() //TODO consider LinkedHashSet
            );
        System.exit(0);
    }

    private static void mergeLoggingConfiguration() throws IOException {
        var properties = new Properties();
        loadDefaults(properties);
        loadUserDefinedConfigFile(properties);
        System.setProperty(CONFIG_FILE_PROPERTY, mergedConfigPath(properties));
    }

    private static String mergedConfigPath(Properties properties) throws IOException {
        var path = Files.createTempFile(null, null);
        try (var outputStream = Files.newOutputStream(path)) {
            properties.store(outputStream, null);
        }
        return path.toString();
    }

    private static void loadUserDefinedConfigFile(Properties properties) throws IOException {
        var configFile = System.getProperty(CONFIG_FILE_PROPERTY);
        if (configFile == null) {
            return;
        }
        try (var inputStream = Files.newInputStream(Paths.get(configFile))) {
            properties.load(inputStream);
        }
    }

    private static void loadDefaults(Properties properties) throws IOException {
        try (var inputStream = loggingPropertiesInputStream()) {
            properties.load(inputStream);
        }
    }

    private static InputStream loggingPropertiesInputStream() {
        return Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("logging.properties");
    }
}
