package com.github.maximtereshchenko.conveyor.api.port;

import java.util.Map;

public record PluginDefinition(String name, int version, Map<String, String> configuration) {}
