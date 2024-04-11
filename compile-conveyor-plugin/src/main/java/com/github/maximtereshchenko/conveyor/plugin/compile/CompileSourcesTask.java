package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Set;

final class CompileSourcesTask extends CompileJavaFilesTask {

    CompileSourcesTask(ConveyorSchematic schematic, Path outputDirectory) {
        super(schematic, ProductType.SOURCE, outputDirectory, ProductType.EXPLODED_MODULE);
    }

    @Override
    Set<Path> dependencies(ConveyorSchematic schematic, Set<Product> products) {
        return schematic.modulePath(Set.of(DependencyScope.IMPLEMENTATION));
    }
}
