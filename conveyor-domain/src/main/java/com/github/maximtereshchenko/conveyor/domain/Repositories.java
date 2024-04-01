package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinition;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionTranslator;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class Repositories {

    private final Set<Repository> all;
    private final SchematicDefinitionTranslator schematicDefinitionTranslator;

    Repositories(Set<Repository> all, SchematicDefinitionTranslator schematicDefinitionTranslator) {
        this.all = all;
        this.schematicDefinitionTranslator = schematicDefinitionTranslator;
    }

    SchematicDefinition schematicDefinition(
        String group,
        String name,
        SemanticVersion semanticVersion
    ) {
        return schematicDefinitionTranslator.schematicDefinition(
            path(group, name, semanticVersion, Repository.Classifier.SCHEMATIC_DEFINITION)
        );
    }

    Path module(
        String group,
        String name,
        SemanticVersion semanticVersion
    ) {
        return path(group, name, semanticVersion, Repository.Classifier.MODULE);
    }

    private Path path(
        String group,
        String name,
        SemanticVersion semanticVersion,
        Repository.Classifier classifier
    ) {
        return all.stream()
            .map(repository -> repository.path(group, name, semanticVersion, classifier))
            .flatMap(Optional::stream)
            .findAny()
            .orElseThrow();
    }
}
