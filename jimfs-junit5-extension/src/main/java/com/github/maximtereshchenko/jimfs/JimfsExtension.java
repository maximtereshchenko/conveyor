package com.github.maximtereshchenko.jimfs;

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

public final class JimfsExtension implements ParameterResolver {

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
            return Files.createDirectories(jimfsFileSystem(extensionContext).getPath("/test"));
        } catch (IOException e) {
            throw new ParameterResolutionException("Could not create virtual directory", e);
        }
    }

    private FileSystem jimfsFileSystem(ExtensionContext extensionContext) {
        var store = extensionContext.getStore(namespace);
        var fileSystem = store.getOrComputeIfAbsent(
            FileSystem.class,
            key -> Jimfs.newFileSystem(Configuration.unix()),
            FileSystem.class
        );
        store.put(
            ExtensionContext.Store.CloseableResource.class,
            (ExtensionContext.Store.CloseableResource) fileSystem::close
        );
        return fileSystem;
    }
}
