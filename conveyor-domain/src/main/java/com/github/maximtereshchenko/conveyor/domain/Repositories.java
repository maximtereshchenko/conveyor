package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DefinitionTranslator;
import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class Repositories {

    private final Set<Repository> all;
    private final DefinitionTranslator definitionTranslator;

    Repositories(Set<Repository> all, DefinitionTranslator definitionTranslator) {
        this.all = all;
        this.definitionTranslator = definitionTranslator;
    }

    ManualDefinition manualDefinition(String group, String name, SemanticVersion semanticVersion) {
        return definitionTranslator.manualDefinition(
            path(
                uri(group, name, semanticVersion, "json"),
                Repository.Classifier.SCHEMATIC_DEFINITION
            )
        );
    }

    Path path(String group, String name, SemanticVersion semanticVersion) {
        return path(
            uri(group, name, semanticVersion, "jar"),
            Repository.Classifier.MODULE
        );
    }

    private Path path(URI uri, Repository.Classifier classifier) {
        return all.stream()
            .map(repository -> repository.path(uri, classifier))
            .flatMap(Optional::stream)
            .findAny()
            .orElseThrow();
    }

    private URI uri(String group, String name, SemanticVersion semanticVersion, String extension) {
        return URI.create(
            "%s/%s/%s/%s-%s.%s".formatted(
                group.replace('.', '/'),
                name,
                semanticVersion,
                name,
                semanticVersion,
                extension
            )
        );
    }
}
