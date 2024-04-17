package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public interface ConveyorSchematic {

    SchematicCoordinates coordinates();

    Path discoveryDirectory();

    Path constructionDirectory();

    Optional<String> propertyValue(String key);

    Set<Path> modulePath(Set<DependencyScope> scopes);
}
