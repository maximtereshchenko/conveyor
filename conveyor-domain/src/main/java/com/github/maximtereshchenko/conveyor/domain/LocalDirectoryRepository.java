package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class LocalDirectoryRepository implements Repository {

    private final Path path;
    private final DefinitionReader definitionReader;

    LocalDirectoryRepository(Path path, DefinitionReader definitionReader) {
        this.path = path;
        this.definitionReader = definitionReader;
    }

    @Override
    public Optional<ManualDefinition> manualDefinition(
        String name,
        SemanticVersion semanticVersion
    ) {
        return path(fullName(name, semanticVersion) + ".json")
            .map(definitionReader::manualDefinition);
    }

    @Override
    public Optional<Path> path(String name, SemanticVersion semanticVersion) {
        return path(fullName(name, semanticVersion) + ".jar");
    }

    private Optional<Path> path(String fileName) {
        var requested = path.resolve(fileName).normalize();
        if (Files.exists(requested)) {
            return Optional.of(requested);
        }
        return Optional.empty();
    }

    private String fullName(String name, SemanticVersion version) {
        return name + '-' + version;
    }
}
