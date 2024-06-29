package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class PropertiesModel {

    private final Map<String, Value> all;

    private PropertiesModel(Map<String, Value> all) {
        this.all = all;
    }

    static PropertiesModel from(Map<String, String> properties) {
        return new PropertiesModel(
            properties.entrySet()
                .stream()
                .collect(
                    Collectors.toMap(Map.Entry::getKey, entry -> new StringValue(entry.getValue()))
                )
        );
    }

    PropertiesModel with(SchematicPropertyKey schematicPropertyKey, String value) {
        var copy = new HashMap<>(all);
        copy.put(schematicPropertyKey.fullName(), new StringValue(value));
        return new PropertiesModel(copy);
    }

    PropertiesModel override(PropertiesModel base) {
        var copy = new HashMap<>(base.all);
        copy.putAll(all);
        return new PropertiesModel(copy);
    }

    PropertiesModel withResolvedPath(
        SchematicPropertyKey schematicPropertyKey,
        Path path,
        Path defaultValue
    ) {
        var copy = new HashMap<>(all);
        copy.put(
            schematicPropertyKey.fullName(),
            new PathValue(
                value(schematicPropertyKey.fullName())
                    .map(path::resolve)
                    .orElse(defaultValue)
                    .normalize()
            )
        );
        return new PropertiesModel(copy);
    }

    Optional<String> value(String key) {
        return Optional.ofNullable(all.get(key))
            .map(Value::toString);
    }

    Path path(SchematicPropertyKey schematicPropertyKey) {
        return switch (all.get(schematicPropertyKey.fullName())) {
            case PathValue pathValue -> pathValue.path();
            case StringValue ignored -> throw new IllegalArgumentException();
        };
    }

    private sealed interface Value {}

    private record PathValue(Path path) implements Value {

        @Override
        public String toString() {
            return path.toString();
        }
    }

    private record StringValue(String string) implements Value {

        @Override
        public String toString() {
            return string;
        }
    }
}
