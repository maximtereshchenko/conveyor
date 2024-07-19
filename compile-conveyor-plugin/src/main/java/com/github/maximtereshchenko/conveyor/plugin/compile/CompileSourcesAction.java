package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class CompileSourcesAction implements ConveyorTaskAction {

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
    public void execute(ConveyorTaskTracer tracer) {
        if (Files.exists(sourcesDirectory)) {
            compiler.compile(
                new FileTree(sourcesDirectory).files(),
                classpath,
                outputDirectory,
                diagnostic -> tracer.submit(TracingImportance.WARN, diagnostic::toString)
            );
            tracer.submit(TracingImportance.INFO, () -> "Compiled classes to " + outputDirectory);
        } else {
            tracer.submit(
                TracingImportance.WARN,
                () -> "No sources to compile at " + sourcesDirectory
            );
        }
    }
}
