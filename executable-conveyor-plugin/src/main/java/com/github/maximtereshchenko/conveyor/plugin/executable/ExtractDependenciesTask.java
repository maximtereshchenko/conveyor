package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class ExtractDependenciesTask implements ConveyorTask {

    private static final System.Logger LOGGER =
        System.getLogger(ExtractDependenciesTask.class.getName());

    private final ConveyorSchematic schematic;
    private final Path classesDirectory;

    ExtractDependenciesTask(ConveyorSchematic schematic, Path classesDirectory) {
        this.schematic = schematic;
        this.classesDirectory = classesDirectory;
    }

    @Override
    public String name() {
        return "extract-dependencies";
    }

    @Override
    public Optional<Path> execute() {
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
