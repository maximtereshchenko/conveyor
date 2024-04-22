package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.InputStream;
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
    public Optional<Path> artifact(URI uri) {
        var requested = Paths.get(uri);
        if (Files.exists(requested)) {
            return Optional.of(requested);
        }
        return Optional.empty();
    }

    Path publish(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier,
        InputStream inputStream
    ) {
        var destination = Paths.get(uri(id, semanticVersion, classifier));
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
}
