package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
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
                (dependencies, products) ->
                    execute(products, properties.constructionDirectory().resolve("module-path"))
            )
        );
    }

    private Products execute(Products products, Path path) {
        return products.with(
            write(
                path,
                ServiceLoader.load(getClass().getModule().getLayer(), Supplier.class)
                    .stream()
                    .map(ServiceLoader.Provider::get)
                    .map(Supplier::get)
                    .map(Object::toString)
                    .collect(Collectors.joining(System.lineSeparator()))
            ),
            ProductType.MODULE_COMPONENT
        );
    }

    private Path write(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            return Files.writeString(path, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
