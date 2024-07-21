package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class ConstructionRepository implements Repository<Path, Path> {

    private final Map<Coordinates, Path> schematicDefinitions = new ConcurrentHashMap<>();
    private final Map<Coordinates, Path> artifacts = new ConcurrentHashMap<>();

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
            case CLASSES -> artifacts.put(coordinates, artifact);
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
            case CLASSES -> Optional.ofNullable(artifacts.get(new Coordinates(id, version)));
            case POM -> Optional.empty();
        };
    }

    private record Coordinates(Id id, Version version) {}
}
