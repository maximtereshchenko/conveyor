package com.github.maximtereshchenko.conveyor.api.port;

import java.util.Objects;

public record ArtifactPreferenceDefinition(String group, String name, String version) {

    public ArtifactPreferenceDefinition {
        Objects.requireNonNull(group);
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
    }
}
