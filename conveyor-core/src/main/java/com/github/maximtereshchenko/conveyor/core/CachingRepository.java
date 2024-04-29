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
    public Optional<Path> artifact(Id id, SemanticVersion semanticVersion, Classifier classifier) {
        return cache.artifact(id, semanticVersion, classifier)
            .or(() ->
                original.artifact(id, semanticVersion, classifier)
                    .flatMap(inputStream -> published(id, semanticVersion, classifier, inputStream))
            );
    }

    @Override
    public void publish(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier,
        Resource resource
    ) {
        original.publish(id, semanticVersion, classifier, resource);
    }

    private Optional<Path> published(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier,
        InputStream inputStream
    ) {
        cache.publish(id, semanticVersion, classifier, () -> inputStream);
        return cache.artifact(id, semanticVersion, classifier);
    }
}
