package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

final class LocalDirectoryRepository extends UriRepository<Path> {

    LocalDirectoryRepository(Path path) {
        super(path.toUri());
    }

    @Override
    public boolean hasName(String name) {
        return false;
    }

    @Override
    Optional<Path> artifact(URI uri) {
        var requested = Paths.get(uri);
        if (Files.exists(requested)) {
            return Optional.of(requested);
        }
        return Optional.empty();
    }

    @Override
    void publish(URI uri, Resource resource) {
        try (var inputStream = resource.inputStream()) {
            var destination = Paths.get(uri);
            Files.deleteIfExists(destination);
            Files.createDirectories(destination.getParent());
            try (var outputStream = Files.newOutputStream(destination)) {
                inputStream.transferTo(outputStream);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
