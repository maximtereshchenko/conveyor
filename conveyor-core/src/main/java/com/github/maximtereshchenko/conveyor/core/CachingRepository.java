package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;

final class CachingRepository implements Repository<Path> {

    private final Repository<InputStream> original;
    private final LocalDirectoryRepository cache;

    CachingRepository(Repository<InputStream> original, LocalDirectoryRepository cache) {
        this.original = original;
        this.cache = cache;
    }

    @Override
    public Optional<Path> artifact(Id id, SemanticVersion semanticVersion, Classifier classifier) {
        return cache.artifact(id, semanticVersion, classifier)
            .or(() ->
                original.artifact(id, semanticVersion, classifier)
                    .map(inputStream -> published(id, semanticVersion, classifier, inputStream))
            );
    }

    private Path published(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier,
        InputStream inputStream
    ) {
        try (inputStream) {
            return cache.publish(id, semanticVersion, classifier, inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
