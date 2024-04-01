package com.github.maximtereshchenko.conveyor.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

record PluginModel(String name, Optional<String> version, Map<String, String> configuration) {

    PluginModel override(PluginModel base) {
        var copy = new HashMap<>(base.configuration());
        copy.putAll(configuration);
        return new PluginModel(name, version.or(base::version), copy);
    }
}
