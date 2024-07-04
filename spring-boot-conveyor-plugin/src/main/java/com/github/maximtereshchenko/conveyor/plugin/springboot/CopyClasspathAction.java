package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.files.FileTree;

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
        if (Files.exists(classesDirectory)) {
            copyClasses();
            copyDependencies();
        }
        return Optional.empty();
    }

    private void copyClasses() {
        var classesDestination = destination.resolve("classes");
        new FileTree(classesDirectory).copyTo(classesDestination);
        LOGGER.log(
            System.Logger.Level.INFO,
            "Copied {0} to {1}",
            classesDirectory,
            classesDestination
        );
    }

    private void copyDependencies() {
        for (var dependency : dependencies) {
            var dependencyDestination = destination.resolve(dependency.getFileName());
            new FileTree(dependency).copyTo(dependencyDestination);
            LOGGER.log(System.Logger.Level.INFO, "Copied {0}", dependencyDestination);
        }
    }
}
