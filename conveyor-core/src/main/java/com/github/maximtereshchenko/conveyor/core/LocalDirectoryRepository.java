package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

final class LocalDirectoryRepository implements UriRepository<Path> {

    private final Path path;

    LocalDirectoryRepository(Path path) {
        this.path = path;
    }

    @Override
    public Optional<Path> artifact(URI uri) {
        var requested = absolutePath(uri);
        if (Files.exists(requested)) {
            return Optional.of(requested);
        }
        return Optional.empty();
    }

    Path publish(URI uri, InputStream inputStream) {
        var destination = absolutePath(uri);
        try {
            Files.deleteIfExists(destination);
            Files.createDirectories(destination.getParent());
            try (var outputStream = Files.newOutputStream(destination)) {
                inputStream.transferTo(outputStream);
                return destination;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path absolutePath(URI uri) {
        return Paths.get(URI.create(path.toUri().toString() + '/' + uri));
    }
}
