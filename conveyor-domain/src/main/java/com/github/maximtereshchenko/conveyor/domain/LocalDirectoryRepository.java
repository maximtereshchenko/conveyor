package com.github.maximtereshchenko.conveyor.domain;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

final class LocalDirectoryRepository implements Repository {

    private final Path path;

    LocalDirectoryRepository(Path path) {
        this.path = path;
    }

    @Override
    public Optional<Path> path(URI uri, Classifier classifier) {
        var requested = absolutePath(uri);
        if (Files.exists(requested)) {
            return Optional.of(requested);
        }
        return Optional.empty();
    }

    void stored(URI uri, IOConsumer<OutputStream> consumer) {
        try (var outputStream = outputStream(absolutePath(uri))) {
            consumer.accept(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private OutputStream outputStream(Path target) throws IOException {
        Files.createDirectories(target.getParent());
        return Files.newOutputStream(target);
    }

    private Path absolutePath(URI uri) {
        return Paths.get(URI.create(path.toUri().toString() + '/' + uri));
    }
}
