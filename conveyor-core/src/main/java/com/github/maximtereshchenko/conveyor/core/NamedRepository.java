package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

final class NamedRepository implements Repository<Path, Path> {

    private final String name;
    private final Repository<Path, Path> original;

    NamedRepository(String name, Repository<Path, Path> original) {
        this.name = name;
        this.original = original;
    }

    @Override
    public void publish(Id id, Version version, Classifier classifier, Path artifact) {
        original.publish(id, version, classifier, artifact);
    }

    @Override
    public Optional<Path> artifact(Id id, Version version, Classifier classifier) {
        return original.artifact(id, version, classifier);
    }

    String name() {
        return name;
    }
}
