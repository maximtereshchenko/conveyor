package com.github.maximtereshchenko.conveyor.plugin.api;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public interface ConveyorSchematic {

    String group();

    String name();

    String version();

    Path path();

    Optional<String> propertyValue(String key);

    Set<Path> classpath(Set<ClasspathScope> scopes);

    void publish(String repository, Path path, ArtifactClassifier artifactClassifier);
}
