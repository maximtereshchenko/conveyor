package com.github.maximtereshchenko.conveyor.common.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

final class DirectoryEntriesProvider implements ArgumentsProvider {

    private final ExtensionContext.Namespace namespace =
        ExtensionContext.Namespace.create(DirectoryEntriesProvider.class);

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        var directory = Paths.get("directory");
        var first = Paths.get("first");
        var second = Paths.get("second");
        var nested = directory.resolve(first);
        return Stream.of(
                Set.<Path>of(),
                Set.of(first),
                Set.of(first, second),
                Set.of(nested),
                Set.of(nested, directory.resolve(second)),
                Set.of(nested, second)
            )
            .map(relative -> temporaryDirectory(relative, context))
            .map(Arguments::arguments);
    }

    private Path temporaryDirectory(Set<Path> relative, ExtensionContext context) {
        try {
            var directory = Files.createTempDirectory(null);
            context.getStore(namespace).put(directory, new TemporaryDirectory(directory));
            for (var path : relative) {
                var destination = directory.resolve(path);
                Files.createDirectories(destination.getParent());
                Files.createFile(destination);
            }
            return directory;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
