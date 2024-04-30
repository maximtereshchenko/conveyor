package com.github.maximtereshchenko.conveyor.cli;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.core.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.LogManager;

final class Main {

    public static void main(String[] args) throws IOException {
        try (var inputStream = loggingPropertiesInputStream()) {
            LogManager.getLogManager()
                .updateConfiguration(inputStream, key -> Objects::requireNonNullElse);
        }
        new ConveyorFacade(JacksonAdapter.configured())
            .construct(
                Paths.get(args[0]).toAbsolutePath().normalize(),
                Stage.valueOf(args[1])
            );
    }

    private static InputStream loggingPropertiesInputStream() {
        return Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("logging.properties");
    }
}
