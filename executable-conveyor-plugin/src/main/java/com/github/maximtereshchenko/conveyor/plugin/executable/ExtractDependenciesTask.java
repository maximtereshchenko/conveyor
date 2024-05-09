package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.nio.file.Path;
import java.util.Set;

final class ExtractDependenciesTask extends BaseTask {

    private static final System.Logger LOGGER =
        System.getLogger(ExtractDependenciesTask.class.getName());

    ExtractDependenciesTask(ConveyorSchematic schematic) {
        super(schematic);
    }

    @Override
    public String name() {
        return "extract-dependencies";
    }

    @Override
    void onExplodedJar(ConveyorSchematic schematic, Path explodedJar) {
        for (var dependency : schematic.classPath(Set.of(DependencyScope.IMPLEMENTATION))) {
            new ZipArchive(dependency).extract(explodedJar);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Extracted {0} to {1}",
                dependency,
                explodedJar
            );
        }
    }
}
