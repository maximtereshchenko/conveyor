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
    public List<ConveyorTaskBinding> bindings(ConveyorSchematic schematic, Map<String, String> configuration) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.RUN,
                (conveyorSchematic, products) -> execute(schematic, configuration)
            )
        );
    }

    private Set<Product> execute(ConveyorSchematic schematic, Map<String, String> configuration) {
        writeProperties(schematic, Set.of(configuration.get("keys").split(",")));
        return Set.of();
    }

    private void writeProperties(ConveyorSchematic schematic, Set<String> keys) {
        try {
            Files.writeString(
                Files.createDirectories(schematic.constructionDirectory()).resolve("properties"),
                keys.stream()
                    .map(key ->
                        schematic.propertyValue(key)
                            .map(value -> key + '=' + value)
                    )
                    .flatMap(Optional::stream)
                    .collect(Collectors.joining(System.lineSeparator()))
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
