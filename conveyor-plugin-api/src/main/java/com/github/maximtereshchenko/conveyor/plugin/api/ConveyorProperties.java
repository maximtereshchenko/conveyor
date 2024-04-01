package com.github.maximtereshchenko.conveyor.plugin.api;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class ConveyorProperties {

    private final Map<String, String> properties;
    private final String schematicNameKey;
    private final String discoveryDirectoryKey;
    private final String constructionDirectoryKey;

    public ConveyorProperties(
        Map<String, String> properties,
        String schematicNameKey,
        String discoveryDirectoryKey,
        String constructionDirectoryKey
    ) {
        this.properties = Map.copyOf(properties);
        this.schematicNameKey = schematicNameKey;
        this.discoveryDirectoryKey = discoveryDirectoryKey;
        this.constructionDirectoryKey = constructionDirectoryKey;
    }

    public String schematicName() {
        return properties.get(schematicNameKey);
    }

    public Path discoveryDirectory() {
        return path(discoveryDirectoryKey);
    }

    public Path constructionDirectory() {
        return path(constructionDirectoryKey);
    }

    private Path path(String key) {
        return Paths.get(properties.get(key));
    }
}
