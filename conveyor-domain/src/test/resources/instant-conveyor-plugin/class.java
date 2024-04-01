package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public final class ${normalizedName} implements ConveyorPlugin {

    @Override
    public String name() {
        return "${name}";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(ConveyorProperties properties, Map<String, String> configuration) {
        var stage = Stage.valueOf(configuration.getOrDefault("stage", "COMPILE"));
        return List.of(
            new ConveyorTaskBinding(
                stage,
                Step.FINALIZE,
                (dependencies, products) ->
                    execute(products, properties.constructionDirectory().resolve("${name}-finalize"))
            ),
            new ConveyorTaskBinding(
                stage,
                Step.PREPARE,
                (dependencies, products) ->
                    execute(products, properties.constructionDirectory().resolve("${name}-prepare"))
            ),
            new ConveyorTaskBinding(
                stage,
                Step.RUN,
                (dependencies, products) ->
                    execute(products, properties.constructionDirectory().resolve("${name}-run"))
            )
        );
    }

    private Products execute(Products products, Path path) {
        sleep();
        return products.with(writeInstant(path), ProductType.MODULE_COMPONENT);
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Path writeInstant(Path path) {
        try {
            Files.createDirectories(path.getParent());
            return Files.writeString(path, Instant.now().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
