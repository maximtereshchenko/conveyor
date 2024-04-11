package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

abstract class CompileJavaFilesTask implements ConveyorTask {

    private final ConveyorSchematic schematic;
    private final ProductType sourceType;
    private final Path outputDirectory;
    private final ProductType outputType;

    CompileJavaFilesTask(
        ConveyorSchematic schematic,
        ProductType sourceType,
        Path outputDirectory,
        ProductType outputType
    ) {
        this.schematic = schematic;
        this.sourceType = sourceType;
        this.outputDirectory = outputDirectory;
        this.outputType = outputType;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        var sources = sources(products);
        if (sources.isEmpty()) {
            return Set.of();
        }
        if (!compile(dependencies(schematic, products), sources)) {
            throw new IllegalArgumentException("Could not compile");
        }
        return Set.of(schematic.product(outputDirectory, outputType));
    }

    abstract Set<Path> dependencies(ConveyorSchematic schematic, Set<Product> products);

    private Set<Path> sources(Set<Product> products) {
        return products.stream()
            .filter(product -> product.type() == sourceType)
            .map(Product::path)
            .collect(Collectors.toSet());
    }

    private StandardJavaFileManager standardFileManager(JavaCompiler compiler) {
        var standardFileManager = compiler.getStandardFileManager(
            System.err::println,
            Locale.ROOT,
            StandardCharsets.UTF_8
        );
        standardFileManager.setPathFactory(
            new SecondaryFileSystemPathFactory(outputDirectory.getFileSystem())
        );
        return standardFileManager;
    }

    private boolean compile(Set<Path> dependencies, Set<Path> sources) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = standardFileManager(compiler);
        return compiler.getTask(
                new PrintWriter(System.err),
                fileManager,
                System.err::println,
                List.of(
                    "--module-path", modulePath(dependencies),
                    "-d", outputDirectory.toString()
                ),
                List.of(),
                fileManager.getJavaFileObjectsFromPaths(sources)
            )
            .call();
    }

    private String modulePath(Set<Path> dependencies) {
        return dependencies.stream()
            .map(Path::toString)
            .collect(Collectors.joining(":"));
    }
}
