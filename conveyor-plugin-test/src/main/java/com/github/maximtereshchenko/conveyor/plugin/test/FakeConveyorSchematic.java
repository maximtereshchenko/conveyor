package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class FakeConveyorSchematic implements ConveyorSchematic {

    private final Path path;
    private final Set<Path> dependencies;
    private final List<PublishedArtifact> published = new ArrayList<>();

    private FakeConveyorSchematic(Path path, Set<Path> dependencies) {
        this.path = path;
        this.dependencies = dependencies;
    }

    public static FakeConveyorSchematic from(Path directory, Path... dependencies)
        throws IOException {
        return from(directory, Set.of(dependencies));
    }

    public static FakeConveyorSchematic from(Path directory, Set<Path> dependencies)
        throws IOException {
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
        return dependencies;
    }

    @Override
    public void publish(String repository, Path path, ArtifactClassifier artifactClassifier) {
        published.add(new PublishedArtifact(repository, path, artifactClassifier));
    }

    public List<PublishedArtifact> published() {
        return published;
    }
}
