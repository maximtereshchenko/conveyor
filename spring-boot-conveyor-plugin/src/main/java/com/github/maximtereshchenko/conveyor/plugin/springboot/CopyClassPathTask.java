package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class CopyClassPathTask extends BaseTask {

    private static final System.Logger LOGGER = System.getLogger(CopyClassPathTask.class.getName());

    private final Path destination;

    CopyClassPathTask(ConveyorSchematic schematic, Path destination) {
        super(schematic);
        this.destination = destination;
    }

    @Override
    public String name() {
        return "copy-dependencies";
    }

    @Override
    void onExplodedJar(ConveyorSchematic schematic, Path explodedJar) {
        try {
            var explodedJarDestination = destination.resolve("exploded-jar");
            Files.walkFileTree(
                explodedJar,
                new CopyRecursively(explodedJar, explodedJarDestination)
            );
            LOGGER.log(
                System.Logger.Level.INFO,
                "Copied exploded JAR to {0}",
                explodedJarDestination
            );
            for (var dependency : schematic.classPath(Set.of(DependencyScope.IMPLEMENTATION))) {
                var dependencyDestination = destination.resolve(dependency.getFileName());
                Files.copy(dependency, dependencyDestination);
                LOGGER.log(System.Logger.Level.INFO, "Copied {0}", dependencyDestination);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
