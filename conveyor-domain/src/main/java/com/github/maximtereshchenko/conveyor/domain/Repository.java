package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.nio.file.Path;
import java.util.Optional;

interface Repository {

    Optional<ManualDefinition> manualDefinition(String name, int version);

    Optional<Path> path(String name, int version);
}
