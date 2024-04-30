package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Set;

final class CompileSourcesTask extends CompileJavaFilesTask {

    CompileSourcesTask(ConveyorSchematic schematic, Path outputDirectory, Compiler compiler) {
        super(
            schematic,
            ProductType.SOURCE,
            outputDirectory,
            ProductType.EXPLODED_JAR,
            compiler
        );
    }

    @Override
    public String name() {
        return "compile-sources";
    }

    @Override
    Set<Path> classPath(ConveyorSchematic schematic, Set<Product> products) {
        return schematic.classPath(Set.of(DependencyScope.IMPLEMENTATION));
    }
}
