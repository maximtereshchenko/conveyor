package com.github.maximtereshchenko.compiler;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class Compiler {

    private final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

    public void compile(Set<Path> sources, Set<Path> modulePath, Path outputDirectory) {
        if (Boolean.FALSE.equals(compilationTask(sources, modulePath, outputDirectory).call())) {
            throw new IllegalArgumentException("Could not compile");
        }
    }

    private JavaCompiler.CompilationTask compilationTask(
        Set<Path> sources,
        Set<Path> modulePath,
        Path outputDirectory
    ) {
        var fileManager = standardFileManager(outputDirectory.getFileSystem());
        return javaCompiler.getTask(
            new PrintWriter(System.err),
            fileManager,
            System.err::println,
            List.of(
                "--module-path", modulePath(modulePath),
                "-d", outputDirectory.toString()
            ),
            List.of(),
            fileManager.getJavaFileObjectsFromPaths(sources)
        );
    }

    private StandardJavaFileManager standardFileManager(FileSystem fileSystem) {
        var standardFileManager = javaCompiler.getStandardFileManager(
            System.err::println,
            Locale.ROOT,
            StandardCharsets.UTF_8
        );
        standardFileManager.setPathFactory(new SecondaryFileSystemPathFactory(fileSystem));
        return standardFileManager;
    }

    private String modulePath(Set<Path> dependencies) {
        return dependencies.stream()
            .map(Path::toString)
            .collect(Collectors.joining(":"));
    }
}
