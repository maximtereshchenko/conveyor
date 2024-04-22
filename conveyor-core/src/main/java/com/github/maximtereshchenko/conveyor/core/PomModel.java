package com.github.maximtereshchenko.conveyor.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

record PomModel(
    Optional<Parent> parent,
    Optional<String> groupId,
    String artifactId,
    Optional<String> version,
    Map<String, String> properties,
    List<Reference> dependencyManagement,
    List<Reference> dependencies
) {

    enum ReferenceType {
        POM, JAR
    }

    enum ReferenceScope {
        COMPILE, RUNTIME, TEST, SYSTEM, PROVIDED
    }

    record Parent(String groupId, String artifactId, String version) {}

    record Reference(
        String groupId,
        String artifactId,
        String version,
        Optional<ReferenceType> type,
        Optional<ReferenceScope> scope
    ) {}
}
