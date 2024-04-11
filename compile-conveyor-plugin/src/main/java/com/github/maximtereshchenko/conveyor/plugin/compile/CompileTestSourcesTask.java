package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class CompileTestSourcesTask extends CompileJavaFilesTask {

    CompileTestSourcesTask(ConveyorSchematic schematic, Path outputDirectory) {
        super(
            schematic,
            ProductType.TEST_SOURCE,
            outputDirectory,
            ProductType.EXPLODED_TEST_MODULE
        );
    }

    @Override
    Set<Path> dependencies(ConveyorSchematic schematic, Set<Product> products) {
        return Stream.concat(
                schematic.modulePath(Set.of(DependencyScope.IMPLEMENTATION, DependencyScope.TEST))
                    .stream(),
                products.stream()
                    .filter(product -> product.type() == ProductType.EXPLODED_MODULE)
                    .map(Product::path)
            )
            .collect(Collectors.toSet());
    }
}
