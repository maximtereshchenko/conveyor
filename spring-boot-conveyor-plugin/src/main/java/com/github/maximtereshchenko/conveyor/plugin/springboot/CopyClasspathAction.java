package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.filevisitors.Copy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

final class CopyClasspathAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(CopyClasspathAction.class.getName());

    private final Path classesDirectory;
    private final Set<Path> dependencies;
    private final Path destination;

    CopyClasspathAction(Path classesDirectory, Set<Path> dependencies, Path destination) {
        this.classesDirectory = classesDirectory;
        this.dependencies = dependencies;
        this.destination = destination;
    }

    @Override
    public Optional<Path> get() {
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
        Files.walkFileTree(classesDirectory, new Copy(classesDirectory, classesDestination));
        LOGGER.log(
            System.Logger.Level.INFO,
            "Copied {0} to {1}",
            classesDirectory,
            classesDestination
        );
    }

    private void copyDependencies() throws IOException {
        for (var dependency : dependencies) {
            var dependencyDestination = destination.resolve(dependency.getFileName());
            Files.copy(dependency, dependencyDestination);
            LOGGER.log(System.Logger.Level.INFO, "Copied {0}", dependencyDestination);
        }
    }
}
