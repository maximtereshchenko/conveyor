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
                (dependencies, products) ->
                    execute(
                        products,
                        properties.constructionDirectory(),
                        dependencies
                    )
            )
        );
    }

    private Products execute(Products products, Path path, ConveyorSchematicDependencies dependencies) {
        return products.with(
                write(
                    path.resolve("dependencies-implementation"),
                    string(dependencies, DependencyScope.IMPLEMENTATION)
                ),
                ProductType.MODULE_COMPONENT
            )
            .with(
                write(
                    path.resolve("dependencies-test"),
                    string(dependencies, DependencyScope.TEST)
                ),
                ProductType.MODULE_COMPONENT
            );
    }

    private String string(ConveyorSchematicDependencies dependencies, DependencyScope scope) {
        return dependencies.modulePath(scope)
            .stream()
            .map(Path::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private Path write(Path path, String content){
        try {
            Files.createDirectories(path.getParent());
            return Files.writeString(path, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
