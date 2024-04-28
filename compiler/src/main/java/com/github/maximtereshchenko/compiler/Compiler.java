package com.github.maximtereshchenko.compiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class Compiler {

    private final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

    public void compile(Set<Path> sources, Set<Path> classPath, Path outputDirectory) {
        if (Boolean.FALSE.equals(compilationTask(sources, classPath, outputDirectory).call())) {
            throw new IllegalArgumentException("Could not compile");
        }
    }

    private JavaCompiler.CompilationTask compilationTask(
        Set<Path> sources,
        Set<Path> classPath,
        Path outputDirectory
    ) {
        var fileManager = javaCompiler.getStandardFileManager(
            System.err::println,
            Locale.ROOT,
            StandardCharsets.UTF_8
        );
        return javaCompiler.getTask(
            new PrintWriter(System.err),
            fileManager,
            System.err::println,
            List.of(
                "--class-path", classPathString(classPath),
                "-d", outputDirectory.toString()
            ),
            List.of(),
            fileManager.getJavaFileObjectsFromPaths(sources)
        );
    }

    private String classPathString(Set<Path> dependencies) {
        return dependencies.stream()
            .map(Path::toString)
            .collect(Collectors.joining(":"));
    }
}
