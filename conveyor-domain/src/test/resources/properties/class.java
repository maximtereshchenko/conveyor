package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

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
                (dependencies, products) -> execute(products, properties)
            )
        );
    }

    private Products execute(Products products, ConveyorProperties properties) {
        writeProperties(properties);
        return products;
    }

    private void writeProperties(ConveyorProperties properties) {
        try {
            Files.write(
                Files.createDirectories(properties.constructionDirectory()).resolve("properties"),
                properties.stream()
                    .map(Map.Entry::toString)
                    .toList()
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
