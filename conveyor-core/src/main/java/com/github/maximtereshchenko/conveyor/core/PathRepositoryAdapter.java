package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

final class PathRepositoryAdapter implements Repository<Path, Path> {

    private final Repository<Resource, Path> original;

    PathRepositoryAdapter(Repository<Resource, Path> original) {
        this.original = original;
    }

    @Override
    public void publish(Id id, Version version, Classifier classifier, Path artifact) {
        original.publish(id, version, classifier, new Resource(artifact));
    }

    @Override
    public Optional<Path> artifact(Id id, Version version, Classifier classifier) {
        return original.artifact(id, version, classifier);
    }
}
