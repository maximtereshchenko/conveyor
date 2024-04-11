package com.github.maximtereshchenko.conveyor.plugin.resources.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

final class JimfsExtension implements ParameterResolver {

    private final Namespace namespace = Namespace.create(JimfsExtension.class);

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) {
        return parameterContext.getParameter().getType() == Path.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) {
        try {
            return Files.createDirectories(
                extensionContext.getStore(namespace)
                    .getOrComputeIfAbsent(
                        ClosableFileSystem.class,
                        ignored -> new ClosableFileSystem(Jimfs.newFileSystem(Configuration.unix())),
                        ClosableFileSystem.class
                    )
                    .fileSystem()
                    .getPath("/test")
            );
        } catch (IOException e) {
            throw new ParameterResolutionException("Could not create virtual directory", e);
        }
    }

    private record ClosableFileSystem(FileSystem fileSystem)
        implements ExtensionContext.Store.CloseableResource {

        @Override
        public void close() throws Throwable {
            fileSystem.close();
        }
    }
}
