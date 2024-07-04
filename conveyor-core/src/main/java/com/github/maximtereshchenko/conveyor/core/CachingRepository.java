package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

final class CachingRepository implements Repository<Path> {

    private final Repository<Resource> original;
    private final LocalDirectoryRepository cache;

    CachingRepository(Repository<Resource> original, LocalDirectoryRepository cache) {
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
                    .flatMap(resource -> published(id, version, classifier, resource))
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
        Resource resource
    ) {
        cache.publish(id, version, classifier, resource);
        return cache.artifact(id, version, classifier);
    }
}
