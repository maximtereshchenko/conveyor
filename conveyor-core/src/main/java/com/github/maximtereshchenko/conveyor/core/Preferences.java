package com.github.maximtereshchenko.conveyor.core;

import java.util.Map;
import java.util.Optional;

final class Preferences {

    private final Map<Id, Version> versions;

    Preferences(Map<Id, Version> versions) {
        this.versions = versions;
    }

    Optional<Version> version(Id id) {
        return Optional.ofNullable(versions.get(id));
    }
}
