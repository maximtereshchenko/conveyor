package com.github.maximtereshchenko.conveyor.core;

import java.util.Map;
import java.util.Optional;

final class Preferences {

    private final Map<Id, SemanticVersion> versions;

    Preferences(Map<Id, SemanticVersion> versions) {
        this.versions = versions;
    }

    Optional<SemanticVersion> version(Id id) {
        return Optional.ofNullable(versions.get(id));
    }
}
