package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class ConstructionRepository implements Repository<Path, Path> {

    private final Map<Coordinates, Path> schematicDefinitions = new HashMap<>();
    private final Map<Coordinates, Path> artifacts = new HashMap<>();

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        Path artifact
    ) {
        var coordinates = new Coordinates(id, version);
        switch (classifier) {
            case SCHEMATIC_DEFINITION -> schematicDefinitions.put(coordinates, artifact);
            case JAR -> artifacts.put(coordinates, artifact);
            case POM -> throw new IllegalArgumentException();
        }
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

    private record Coordinates(Id id, Version version) {}
}
