package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.*;

import java.nio.file.Path;
import java.util.stream.Stream;

final class SchematicFactory {

    private final DefinitionReader definitionReader;

    SchematicFactory(DefinitionReader definitionReader) {
        this.definitionReader = definitionReader;
    }

    ImmutableList<Schematic> schematics(Path path) {
        var schematicDefinition = definitionReader.schematicDefinition(path);
        var repository = new Repository(schematicDefinition.repository(), definitionReader);
        return schematics(
            new LocalSchematic(
                schematicTemplate(schematicDefinition.template(), repository),
                schematicDefinition,
                repository,
                path,
                definitionReader
            )
        );
    }

    private ImmutableList<Schematic> schematics(Schematic schematic) {
        return Stream.concat(
                Stream.of(schematic),
                schematic.inclusions()
                    .stream()
                    .map(this::schematics)
                    .flatMap(ImmutableList::stream)
            )
            .sorted(this::compareByMutualDependency)
            .collect(new ImmutableListCollector<>());
    }

    private int compareByMutualDependency(Schematic first, Schematic second) {
        if (first.dependsOn(second)) {
            return 1;
        }
        if (second.dependsOn(first)) {
            return -1;
        }
        return 0;
    }

    private Manual schematicTemplate(TemplateDefinition templateDefinition, Repository repository) {
        return switch (templateDefinition) {
            case ManualTemplateDefinition definition -> manual(
                repository.manualDefinition(definition.name(), definition.version()),
                repository
            );
            case NoExplicitTemplate ignored -> superManual(repository);
        };
    }

    private Manual template(TemplateDefinition templateDefinition, Repository repository) {
        return switch (templateDefinition) {
            case ManualTemplateDefinition definition -> manual(
                repository.manualDefinition(definition.name(), definition.version()),
                repository
            );
            case NoExplicitTemplate ignored -> new EmptyManual();
        };
    }

    private Manual manual(ManualDefinition manualDefinition, Repository repository) {
        return new DefinedManual(template(manualDefinition.template(), repository), manualDefinition, repository);
    }

    private Manual superManual(Repository repository) {
        return template(new ManualTemplateDefinition("super-manual", 1), repository);
    }
}
