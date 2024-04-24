package com.github.maximtereshchenko.conveyor.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

record PomDefinition(
    Optional<Parent> parent,
    Optional<String> groupId,
    String artifactId,
    Optional<String> version,
    Map<String, String> properties,
    List<ManagedDependencyDefinition> dependencyManagement,
    List<DependencyDefinition> dependencies
) {

    enum DependencyScope {
        COMPILE, RUNTIME, TEST, SYSTEM, PROVIDED, IMPORT
    }

    record Parent(String groupId, String artifactId, String version) {}

    record ManagedDependencyDefinition(
        String groupId,
        String artifactId,
        String version,
        Optional<DependencyScope> scope
    ) {}

    record DependencyDefinition(
        String groupId,
        String artifactId,
        Optional<String> version,
        Optional<DependencyScope> scope
    ) {}
}
