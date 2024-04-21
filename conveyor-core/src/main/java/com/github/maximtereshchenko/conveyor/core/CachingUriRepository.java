package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

final class CachingUriRepository implements UriRepository<Path> {

    private final UriRepository<InputStream> original;
    private final LocalDirectoryRepository cache;

    CachingUriRepository(UriRepository<InputStream> original, LocalDirectoryRepository cache) {
        this.original = original;
        this.cache = cache;
    }

    @Override
    public Optional<Path> artifact(URI uri) {
        return cache.artifact(uri)
            .or(() ->
                original.artifact(uri)
                    .map(inputStream -> published(uri, inputStream))
            );
    }

    private Path published(URI uri, InputStream inputStream) {
        try (inputStream) {
            return cache.publish(uri, inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
