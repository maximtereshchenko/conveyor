package com.github.maximtereshchenko.conveyor.api.schematic;

import java.util.Objects;

public record PreferencesInclusionDefinition(String group, String name, String version) {

    public PreferencesInclusionDefinition {
        Objects.requireNonNull(group);
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
    }
}
