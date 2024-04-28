package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

abstract class CompileJavaFilesTask implements ConveyorTask {

    private final ConveyorSchematic schematic;
    private final ProductType sourceType;
    private final Path outputDirectory;
    private final ProductType outputType;
    private final Compiler compiler;

    CompileJavaFilesTask(
        ConveyorSchematic schematic,
        ProductType sourceType,
        Path outputDirectory,
        ProductType outputType,
        Compiler compiler
    ) {
        this.schematic = schematic;
        this.sourceType = sourceType;
        this.outputDirectory = outputDirectory;
        this.outputType = outputType;
        this.compiler = compiler;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        var sources = sources(products);
        if (sources.isEmpty()) {
            return Set.of();
        }
        compiler.compile(sources, classPath(schematic, products), outputDirectory);
        return Set.of(new Product(schematic.coordinates(), outputDirectory, outputType));
    }

    abstract Set<Path> classPath(ConveyorSchematic schematic, Set<Product> products);

    private Set<Path> sources(Set<Product> products) {
        return products.stream()
            .filter(product -> product.schematicCoordinates().equals(schematic.coordinates()))
            .filter(product -> product.type() == sourceType)
            .map(Product::path)
            .collect(Collectors.toSet());
    }
}
