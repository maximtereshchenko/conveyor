package com.github.maximtereshchenko.conveyor.plugin.api;

import java.nio.file.Path;
import java.util.Map;

public record ConveyorPluginConfiguration(Path projectDirectory, Map<String, String> properties) {

    public ConveyorPluginConfiguration {
        properties = Map.copyOf(properties);
    }

    public String value(String key) {
        return properties.get(key);
    }
}
