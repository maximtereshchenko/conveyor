package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;
import com.github.maximtereshchenko.conveyor.api.port.RepositoryDefinition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class Repository {

    private final RepositoryDefinition repositoryDefinition;
    private final DefinitionReader definitionReader;

    Repository(RepositoryDefinition repositoryDefinition, DefinitionReader definitionReader) {
        this.repositoryDefinition = repositoryDefinition;
        this.definitionReader = definitionReader;
    }

    boolean isEnabled() {
        return repositoryDefinition.enabled();
    }

    String name() {
        return repositoryDefinition.name();
    }

    Optional<ManualDefinition> manualDefinition(String name, int version) {
        return path(fullName(name, version) + ".json")
            .map(definitionReader::manualDefinition);
    }

    Optional<Path> path(String name, int version) {
        return path(fullName(name, version) + ".jar");
    }

    private Optional<Path> path(String fileName) {
        var path = repositoryDefinition.path().resolve(fileName);
        if (Files.exists(path)) {
            return Optional.of(path);
        }
        return Optional.empty();
    }

    private String fullName(String name, int version) {
        return name + '-' + version;
    }
}
