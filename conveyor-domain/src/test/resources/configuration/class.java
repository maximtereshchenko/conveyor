package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public final class ${normalizedName} implements ConveyorPlugin {

    @Override
    public String name() {
        return "${name}";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(ConveyorProperties properties, Map<String, String> configuration) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.RUN,
                (dependencies, products) -> execute(products, properties, configuration)
            )
        );
    }

    private Products execute(Products products, ConveyorProperties properties, Map<String, String> configuration) {
        writeConfiguration(properties, configuration);
        return products;
    }

    private void writeConfiguration(ConveyorProperties properties, Map<String, String> configuration) {
        try {
            Files.writeString(
                Files.createDirectories(properties.constructionDirectory()).resolve("configuration"),
                configuration.entrySet()
                    .stream()
                    .map(Map.Entry::toString)
                    .collect(Collectors.joining(System.lineSeparator()))
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
