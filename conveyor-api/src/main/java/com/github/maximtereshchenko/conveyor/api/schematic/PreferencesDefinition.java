package com.github.maximtereshchenko.conveyor.api.schematic;

import java.util.List;
import java.util.Objects;

public record PreferencesDefinition(
    List<PreferencesInclusionDefinition> inclusions,
    List<ArtifactPreferenceDefinition> artifacts
) {

    public PreferencesDefinition {
        inclusions = List.copyOf(Objects.requireNonNullElse(inclusions, List.of()));
        artifacts = List.copyOf(Objects.requireNonNullElse(artifacts, List.of()));
    }

    public PreferencesDefinition() {
        this(List.of(), List.of());
    }
}
