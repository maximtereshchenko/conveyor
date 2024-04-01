package com.github.maximtereshchenko.conveyor.domain.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

final class TestProjectExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == Path.class &&
            parameterContext.isAnnotated(TestProject.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return copyToTemporaryDirectory(
            testProjectPath(
                parameterContext.findAnnotation(TestProject.class)
                    .orElseThrow()
                    .value()
            )
        );
    }

    private Path testProjectPath(String name) {
        try {
            return Paths.get(
                Objects.requireNonNull(
                        Thread.currentThread()
                            .getContextClassLoader()
                            .getResource(name)
                    )
                    .toURI()
            );
        } catch (URISyntaxException e) {
            throw new ParameterResolutionException("Could not find resource", e);
        }
    }

    private Path copyToTemporaryDirectory(Path source) {
        try {
            var tempDirectory = Files.createTempDirectory(null);
            Files.walkFileTree(source, new CopyRecursively(source.getParent(), tempDirectory));
            return tempDirectory.resolve(source.getFileName());
        } catch (IOException e) {
            throw new ParameterResolutionException("Could not copy resource to temporary directory", e);
        }
    }

    private static final class CopyRecursively extends SimpleFileVisitor<Path> {

        private final Path source;
        private final Path destination;

        CopyRecursively(Path source, Path destination) {
            this.source = source;
            this.destination = destination;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Files.createDirectories(destination.resolve(source.relativize(dir)));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, destination.resolve(source.relativize(file)));
            return FileVisitResult.CONTINUE;
        }
    }
}
