package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class CachingClasspathFactory implements ClasspathFactory {

    private final ClasspathFactory original;
    private final Map<Set<Key>, Set<Path>> cache = new HashMap<>();

    CachingClasspathFactory(ClasspathFactory original) {
        this.original = original;
    }

    @Override
    public Set<Path> classpath(Set<? extends Artifact> artifacts) {
        return cache.computeIfAbsent(
            artifacts.stream()
                .map(artifact -> new Key(artifact.id(), artifact.version()))
                .collect(Collectors.toSet()),
            key -> original.classpath(artifacts)
        );
    }

    private record Key(Id id, Version version) {}
}
