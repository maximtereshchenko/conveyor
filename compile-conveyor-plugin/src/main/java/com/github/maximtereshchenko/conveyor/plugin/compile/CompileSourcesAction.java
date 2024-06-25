package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Set;

final class CompileSourcesAction extends CompileJavaFilesAction {

    CompileSourcesAction(
        Path sourcesDirectory,
        Path outputDirectory,
        Compiler compiler,
        ConveyorSchematic schematic
    ) {
        super(sourcesDirectory, outputDirectory, compiler, schematic);
    }

    @Override
    Set<Path> classpath(ConveyorSchematic schematic) {
        return schematic.classpath(Set.of(DependencyScope.IMPLEMENTATION));
    }
}
