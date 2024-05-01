package com.github.maximtereshchenko.conveyor.core;

import java.io.InputStream;
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
    public boolean hasName(String name) {
        return original.hasName(name);
    }

    @Override
    public Optional<Path> artifact(Id id, Version version, Classifier classifier) {
        return cache.artifact(id, version, classifier)
            .or(() ->
                original.artifact(id, version, classifier)
                    .flatMap(inputStream -> published(id, version, classifier, inputStream))
            );
    }

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        Resource resource
    ) {
        original.publish(id, version, classifier, resource);
    }

    private Optional<Path> published(
        Id id,
        Version version,
        Classifier classifier,
        InputStream inputStream
    ) {
        cache.publish(id, version, classifier, () -> inputStream);
        return cache.artifact(id, version, classifier);
    }
}
