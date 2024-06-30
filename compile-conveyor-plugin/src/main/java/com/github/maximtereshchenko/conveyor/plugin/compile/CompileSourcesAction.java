package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.compiler.Compiler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

final class CompileSourcesAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(CompileSourcesAction.class.getName());

    private final Path sourcesDirectory;
    private final Set<Path> classpath;
    private final Path outputDirectory;
    private final Compiler compiler;

    CompileSourcesAction(
        Path sourcesDirectory,
        Set<Path> classpath,
        Path outputDirectory,
        Compiler compiler
    ) {
        this.sourcesDirectory = sourcesDirectory;
        this.classpath = classpath;
        this.outputDirectory = outputDirectory;
        this.compiler = compiler;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(sourcesDirectory)) {
            compiler.compile(files(sourcesDirectory), classpath, outputDirectory);
            LOGGER.log(System.Logger.Level.INFO, "Compiled classes to {0}", outputDirectory);
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "No sources to compile");
        }
        return Optional.empty();
    }

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
