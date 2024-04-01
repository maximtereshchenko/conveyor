package com.github.maximtereshchenko.conveyor.domain;

import java.util.Map;
import java.util.Optional;

final class Preferences {

    private final Map<String, SemanticVersion> versions;

    Preferences(Map<String, SemanticVersion> versions) {
        this.versions = versions;
    }

    Optional<SemanticVersion> version(String group, String name) {
        return Optional.ofNullable(versions.get(group + ':' + name));
    }
}
