package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

abstract class CompileJavaFilesAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(CompileJavaFilesAction.class.getName());

    private final Path sourcesDirectory;
    private final Path outputDirectory;
    private final Compiler compiler;
    private final ConveyorSchematic schematic;

    CompileJavaFilesAction(
        Path sourcesDirectory,
        Path outputDirectory,
        Compiler compiler,
        ConveyorSchematic schematic
    ) {
        this.sourcesDirectory = sourcesDirectory;
        this.outputDirectory = outputDirectory;
        this.compiler = compiler;
        this.schematic = schematic;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(sourcesDirectory)) {
            compiler.compile(files(sourcesDirectory), classpath(schematic), outputDirectory);
            LOGGER.log(System.Logger.Level.INFO, "Compiled classes to {0}", outputDirectory);
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "No sources to compile");
        }
        return Optional.empty();
    }

    abstract Set<Path> classpath(ConveyorSchematic schematic);

    private Set<Path> files(Path directory) {
        try {
            var visitor = new CollectingFileVisitor();
            Files.walkFileTree(directory, visitor);
            return visitor.collected();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
