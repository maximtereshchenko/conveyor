package com.github.maximtereshchenko.conveyor.springboot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class Main {

    public static void main(String[] args) throws Exception {
        var properties = properties();
        new SpringBootLauncher(
            properties.getProperty(Configuration.CLASS_PATH_DIRECTORY_KEY),
            properties.getProperty(Configuration.LAUNCHED_CLASS_NAME_KEY)
        )
            .launch(args);
    }

    private static Properties properties() throws IOException {
        try (var inputStream = inputStream()) {
            var properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }

    private static InputStream inputStream() {
        return Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(Configuration.PROPERTIES_CLASS_PATH_LOCATION);
    }
}
