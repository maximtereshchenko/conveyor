package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class ConstructionRepository implements Repository<Path> {

    private final Map<Coordinates, Path> schematicDefinitions;
    private final Map<Coordinates, Path> artifacts;

    private ConstructionRepository(
        Map<Coordinates, Path> schematicDefinitions,
        Map<Coordinates, Path> artifacts
    ) {
        this.schematicDefinitions = schematicDefinitions;
        this.artifacts = artifacts;
    }

    ConstructionRepository() {
        this(Map.of(), Map.of());
    }

    @Override
    public boolean hasName(String name) {
        return false;
    }

    @Override
    public Optional<Path> artifact(
        Id id,
        Version version,
        Classifier classifier
    ) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION ->
                Optional.ofNullable(schematicDefinitions.get(new Coordinates(id, version)));
            case JAR -> Optional.ofNullable(artifacts.get(new Coordinates(id, version)));
            case POM -> Optional.empty();
        };
    }

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        Resource resource
    ) {
        throw new IllegalArgumentException();
    }

    ConstructionRepository withSchematicDefinition(Id id, Version version, Path path) {
        var copy = new HashMap<>(schematicDefinitions);
        copy.put(new Coordinates(id, version), path);
        return new ConstructionRepository(copy, artifacts);
    }

    ConstructionRepository withArtifact(Id id, Version version, Path path) {
        var copy = new HashMap<>(artifacts);
        copy.put(new Coordinates(id, version), path);
        return new ConstructionRepository(schematicDefinitions, copy);
    }

    private record Coordinates(Id id, Version version) {}
}
