package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

final class CachingRepository implements Repository<Path, Path> {

    private final Repository<Path, Resource> original;
    private final LocalDirectoryRepository cache;

    CachingRepository(Repository<Path, Resource> original, LocalDirectoryRepository cache) {
        this.original = original;
        this.cache = cache;
    }

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        Path artifact
    ) {
        original.publish(id, version, classifier, artifact);
    }

    @Override
    public Optional<Path> artifact(Id id, Version version, Classifier classifier) {
        return cache.artifact(id, version, classifier)
            .or(() ->
                original.artifact(id, version, classifier)
                    .flatMap(artifact -> published(id, version, classifier, artifact))
            );
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
