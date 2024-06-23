package com.github.maximtereshchenko.conveyor.compiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class Compiler {

    private final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

    public void compile(Set<Path> sources, Set<Path> classpath, Path outputDirectory) {
        if (Boolean.FALSE.equals(compilationTask(sources, classpath, outputDirectory).call())) {
            throw new IllegalArgumentException("Could not compile");
        }
    }

    private JavaCompiler.CompilationTask compilationTask(
        Set<Path> sources,
        Set<Path> classpath,
        Path outputDirectory
    ) {
        var diagnosticListener = new LoggingDiagnosticListener();
        var fileManager = javaCompiler.getStandardFileManager(
            diagnosticListener,
            Locale.ROOT,
            StandardCharsets.UTF_8
        );
        return javaCompiler.getTask(
            null,
            fileManager,
            diagnosticListener,
            List.of(
                "--class-path", classpathString(classpath),
                "-d", outputDirectory.toString()
            ),
            List.of(),
            fileManager.getJavaFileObjectsFromPaths(sources)
        );
    }

    private String classpathString(Set<Path> dependencies) {
        return dependencies.stream()
            .map(Path::toString)
            .collect(Collectors.joining(":"));
    }
}
