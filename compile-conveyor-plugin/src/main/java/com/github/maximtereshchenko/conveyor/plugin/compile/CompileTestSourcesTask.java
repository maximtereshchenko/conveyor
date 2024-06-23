package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class CompileTestSourcesTask extends CompileJavaFilesTask {

    private final Path classesDirectory;

    CompileTestSourcesTask(
        Path sourcesDirectory,
        Path outputDirectory,
        Compiler compiler,
        ConveyorSchematic schematic,
        Path classesDirectory
    ) {
        super(sourcesDirectory, outputDirectory, compiler, schematic);
        this.classesDirectory = classesDirectory;
    }

    @Override
    public String name() {
        return "compile-test-sources";
    }

    @Override
    Set<Path> classpath(ConveyorSchematic schematic) {
        return Stream.concat(
                schematic.classpath(Set.of(DependencyScope.IMPLEMENTATION, DependencyScope.TEST))
                    .stream(),
                Stream.of(classesDirectory)
            )
            .collect(Collectors.toSet());
    }
}
