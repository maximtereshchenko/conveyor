package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class CopyClassPathTask implements ConveyorTask {

    private static final System.Logger LOGGER = System.getLogger(CopyClassPathTask.class.getName());

    private final Path classesDirectory;
    private final Path destination;
    private final ConveyorSchematic schematic;

    CopyClassPathTask(Path classesDirectory, Path destination, ConveyorSchematic schematic) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
        this.schematic = schematic;
    }

    @Override
    public String name() {
        return "copy-dependencies";
    }

    @Override
    public Optional<Path> execute() {
        try {
            if (Files.exists(classesDirectory)) {
                copyClasses();
                copyDependencies();
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void copyClasses() throws IOException {
        var classesDestination = destination.resolve("classes");
        Files.walkFileTree(
            classesDirectory,
            new CopyRecursively(classesDirectory, classesDestination)
        );
        LOGGER.log(
            System.Logger.Level.INFO,
            "Copied {0} to {1}",
            classesDirectory,
            classesDestination
        );
    }

    private void copyDependencies() throws IOException {
        for (var dependency : schematic.classpath(Set.of(DependencyScope.IMPLEMENTATION))) {
            var dependencyDestination = destination.resolve(dependency.getFileName());
            Files.copy(dependency, dependencyDestination);
            LOGGER.log(System.Logger.Level.INFO, "Copied {0}", dependencyDestination);
        }
    }
}
