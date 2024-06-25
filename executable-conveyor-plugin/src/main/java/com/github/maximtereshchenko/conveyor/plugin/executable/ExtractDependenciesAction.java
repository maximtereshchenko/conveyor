package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

final class ExtractDependenciesAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(ExtractDependenciesAction.class.getName());

    private final ConveyorSchematic schematic;
    private final Path classesDirectory;

    ExtractDependenciesAction(ConveyorSchematic schematic, Path classesDirectory) {
        this.schematic = schematic;
        this.classesDirectory = classesDirectory;
    }

    @Override
    public Optional<Path> get() {
        for (var dependency : schematic.classpath(Set.of(DependencyScope.IMPLEMENTATION))) {
            new ZipArchive(dependency).extract(classesDirectory);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Extracted {0} to {1}",
                dependency,
                classesDirectory
            );
        }
        return Optional.empty();
    }
}
