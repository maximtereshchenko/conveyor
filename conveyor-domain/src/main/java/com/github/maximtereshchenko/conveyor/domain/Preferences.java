package com.github.maximtereshchenko.conveyor.domain;

import java.util.Map;
import java.util.Optional;

final class Preferences {

    private final Map<String, Integer> versions;

    Preferences(Map<String, Integer> versions) {
        this.versions = versions;
    }

    Optional<Integer> version(String name) {
        return Optional.ofNullable(versions.get(name));
    }
}
