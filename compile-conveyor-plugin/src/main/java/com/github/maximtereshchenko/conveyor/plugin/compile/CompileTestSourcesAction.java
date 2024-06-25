package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class CompileTestSourcesAction extends CompileJavaFilesAction {

    private final Path classesDirectory;

    CompileTestSourcesAction(
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
    Set<Path> classpath(ConveyorSchematic schematic) {
        return Stream.concat(
                schematic.classpath(Set.of(DependencyScope.IMPLEMENTATION, DependencyScope.TEST))
                    .stream(),
                Stream.of(classesDirectory)
            )
            .collect(Collectors.toSet());
    }
}
