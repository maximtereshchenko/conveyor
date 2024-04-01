package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

final class LocalDirectoryRepository extends UriRepository {

    private final Path path;

    LocalDirectoryRepository(Path path) {
        this.path = path;
    }

    void stored(URI uri, IOConsumer<OutputStream> consumer) {
        try (var outputStream = outputStream(absolutePath(uri))) {
            consumer.accept(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    String extension(Classifier classifier) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> "json";
            case MODULE -> "jar";
        };
    }

    @Override
    Optional<Path> path(URI uri, Classifier classifier) {
        var requested = absolutePath(uri);
        if (Files.exists(requested)) {
            return Optional.of(requested);
        }
        return Optional.empty();
    }

    private OutputStream outputStream(Path target) throws IOException {
        Files.createDirectories(target.getParent());
        return Files.newOutputStream(target);
    }

    private Path absolutePath(URI uri) {
        return Paths.get(URI.create(path.toUri().toString() + '/' + uri));
    }
}
