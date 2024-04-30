package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.zip.ZipArchive;

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
    public Set<Product> execute(Set<Product> products) {
        explodedJar(products).ifPresentOrElse(
            this::extractDependencies,
            () -> LOGGER.log(
                System.Logger.Level.WARNING,
                "No destination to extract dependencies to"
            )
        );
        return Set.of();
    }

    private void extractDependencies(Path destination) {
        schematic().classPath(Set.of(DependencyScope.IMPLEMENTATION))
            .forEach(dependency -> new ZipArchive(dependency).extract(destination));
    }
}
