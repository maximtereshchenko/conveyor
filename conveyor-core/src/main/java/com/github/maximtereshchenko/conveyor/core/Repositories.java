package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionTranslator;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;

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

    SchematicDefinition schematicDefinition(Id id, SemanticVersion semanticVersion) {
        return schematicDefinitionTranslator.schematicDefinition(
            path(id, semanticVersion, Repository.Classifier.SCHEMATIC_DEFINITION)
        );
    }

    Path module(Id id, SemanticVersion semanticVersion) {
        return path(id, semanticVersion, Repository.Classifier.MODULE);
    }

    private Path path(Id id, SemanticVersion semanticVersion, Repository.Classifier classifier) {
        return all.stream()
            .map(repository -> repository.path(id, semanticVersion, classifier))
            .flatMap(Optional::stream)
            .findAny()
            .orElseThrow();
    }
}
