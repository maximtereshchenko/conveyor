package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;

final class Repositories {

    private final LinkedHashSet<Repository<Path>> all;
    private final SchematicDefinitionConverter schematicDefinitionConverter;

    Repositories(
        LinkedHashSet<Repository<Path>> all,
        SchematicDefinitionConverter schematicDefinitionConverter
    ) {
        this.all = all;
        this.schematicDefinitionConverter = schematicDefinitionConverter;
    }

    SchematicDefinition schematicDefinition(Id id, Version version) {
        return schematicDefinitionConverter.schematicDefinition(
            path(id, version, Repository.Classifier.SCHEMATIC_DEFINITION)
        );
    }

    Path jar(Id id, Version version) {
        return path(id, version, Repository.Classifier.JAR);
    }

    void publish(
        String repositoryName,
        Id id,
        Version version,
        Repository.Classifier classifier,
        Path path
    ) {
        all.stream()
            .filter(repository -> repository.hasName(repositoryName))
            .forEach(repository ->
                repository.publish(
                    id,
                    version,
                    classifier,
                    () -> Files.newInputStream(path)
                )
            );
    }

    private Path path(Id id, Version version, Repository.Classifier classifier) {
        return all.stream()
            .map(repository -> repository.artifact(id, version, classifier))
            .flatMap(Optional::stream)
            .findAny()
            .orElseThrow(() ->
                new NoSuchElementException("%s:%s:%s".formatted(id.group(), id.name(), version))
            );
    }
}
