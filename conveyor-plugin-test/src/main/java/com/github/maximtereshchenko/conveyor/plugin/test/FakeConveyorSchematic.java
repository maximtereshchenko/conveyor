package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FakeConveyorSchematic implements ConveyorSchematic {

    private final Path path;
    private final Map<Path, DependencyScope> dependencies;
    private final List<PublishedArtifact> published = new ArrayList<>();

    private FakeConveyorSchematic(Path path, Map<Path, DependencyScope> dependencies) {
        this.path = path;
        this.dependencies = dependencies;
    }

    public static FakeConveyorSchematic from(Path directory, Path... dependencies)
        throws IOException {
        return from(directory, Set.of(dependencies));
    }

    public static FakeConveyorSchematic from(Path directory, Set<Path> dependencies)
        throws IOException {
        return from(
            directory,
            dependencies.stream()
                .collect(
                    Collectors.toMap(Function.identity(), path -> DependencyScope.IMPLEMENTATION)
                )
        );
    }

    public static FakeConveyorSchematic from(
        Path directory,
        Map<Path, DependencyScope> dependencies
    ) throws IOException {
        var conveyorJson = directory.resolve("conveyor.json");
        if (!Files.exists(conveyorJson)) {
            Files.createFile(conveyorJson);
        }
        return new FakeConveyorSchematic(conveyorJson, dependencies);
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
    public Set<Path> classpath(Set<DependencyScope> scopes) {
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

    public List<PublishedArtifact> published() {
        return published;
    }
}
