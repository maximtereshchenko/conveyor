package com.github.maximtereshchenko.conveyor.api.port;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record PreferencesDefinition(
    Collection<PreferencesInclusionDefinition> inclusions,
    Collection<ArtifactPreferenceDefinition> artifacts
) {

    public PreferencesDefinition {
        inclusions = List.copyOf(Objects.requireNonNullElse(inclusions, List.of()));
        artifacts = List.copyOf(Objects.requireNonNullElse(artifacts, List.of()));
    }

    public PreferencesDefinition() {
        this(List.of(), List.of());
    }
}
