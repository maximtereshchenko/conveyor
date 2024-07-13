package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ClasspathScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

final class FakeConveyorSchematic implements ConveyorSchematic {

    private final Path path;
    private final Map<Path, ClasspathScope> dependencies;
    private final List<PublishedArtifact> published = new ArrayList<>();

    FakeConveyorSchematic(Path path, Map<Path, ClasspathScope> dependencies) {
        this.path = path;
        this.dependencies = dependencies;
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public Optional<String> propertyValue(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Path> classpath(Set<ClasspathScope> scopes) {
        return dependencies.entrySet()
            .stream()
            .filter(entry -> scopes.contains(entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    @Override
    public void publish(String repository, Path path, ArtifactClassifier artifactClassifier) {
        published.add(new PublishedArtifact(repository, path, artifactClassifier));
    }

    List<PublishedArtifact> published() {
        return published;
    }
}
