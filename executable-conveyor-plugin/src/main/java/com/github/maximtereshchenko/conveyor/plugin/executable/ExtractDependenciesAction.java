package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

final class ExtractDependenciesAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(ExtractDependenciesAction.class.getName());

    private final Set<Path> dependencies;
    private final Path classesDirectory;

    ExtractDependenciesAction(Set<Path> dependencies, Path classesDirectory) {
        this.dependencies = dependencies;
        this.classesDirectory = classesDirectory;
    }

    @Override
    public Optional<Path> get() {
        for (var dependency : dependencies) {
            new ZipArchive(dependency).extract(classesDirectory);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Extracted {0} to {1}",
                dependency,
                classesDirectory
            );
        }
        new FileTree(classesDirectory.resolve("module-info.class")).delete();
        return Optional.empty();
    }
}
