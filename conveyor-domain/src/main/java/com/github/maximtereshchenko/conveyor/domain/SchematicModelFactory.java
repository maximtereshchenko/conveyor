package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionTranslator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SchematicModelFactory {

    private final SchematicDefinitionTranslator schematicDefinitionTranslator;

    SchematicModelFactory(SchematicDefinitionTranslator schematicDefinitionTranslator) {
        this.schematicDefinitionTranslator = schematicDefinitionTranslator;
    }

    LinkedHashSet<HierarchicalLocalSchematicModel> hierarchicalLocalSchematicModels(Path path) {
        return allSchematicPaths(hierarchicalLocalSchematicModel(path).rootPath())
            .map(this::hierarchicalLocalSchematicModel)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    CompleteHierarchicalSchematicModel completeHierarchicalSchematicModel(
        HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel,
        Repositories repositories
    ) {
        return switch (hierarchicalLocalSchematicModel.template()) {
            case NoTemplateModel ignored ->
                new CompleteHierarchicalSchematicModel(hierarchicalLocalSchematicModel);
            case SchematicTemplateModel model -> new CompleteHierarchicalSchematicModel(
                hierarchicalSchematicModel(
                    model.group(),
                    model.name(),
                    model.version(),
                    repositories
                ),
                hierarchicalLocalSchematicModel
            );
        };
    }

    HierarchicalSchematicModel<SchematicModel> hierarchicalSchematicModel(
        String group,
        String name,
        SemanticVersion semanticVersion,
        Repositories repositories
    ) {
        return hierarchicalSchematicModel(
            group,
            name,
            semanticVersion,
            repositories,
            HierarchicalSchematicModel::new
        );
    }

    private HierarchicalSchematicModel<SchematicModel> hierarchicalSchematicModel(
        String group,
        String name,
        SemanticVersion semanticVersion,
        Repositories repositories,
        Function<StandaloneSchematicModel, HierarchicalSchematicModel<SchematicModel>> combiner
    ) {
        var hierarchicalSchematicModel = combiner.apply(
            new StandaloneSchematicModel(
                repositories.schematicDefinition(
                    group,
                    name,
                    semanticVersion
                )
            )
        );
        return switch (hierarchicalSchematicModel.template()) {
            case NoTemplateModel ignored -> hierarchicalSchematicModel;
            case SchematicTemplateModel model -> hierarchicalSchematicModel(
                model.group(),
                model.name(),
                model.version(),
                repositories,
                hierarchicalSchematicModel::inheritedFrom
            );
        };
    }

    private StandaloneLocalSchematicModel standaloneLocalSchematicModel(Path path) {
        return new StandaloneLocalSchematicModel(
            new StandaloneSchematicModel(schematicDefinitionTranslator.schematicDefinition(path)),
            path
        );
    }

    private Stream<Path> allSchematicPaths(Path path) {
        return Stream.concat(
            Stream.of(path),
            standaloneLocalSchematicModel(path)
                .inclusions()
                .stream()
                .flatMap(this::allSchematicPaths)
        );
    }

    private HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel(Path path) {
        return hierarchicalLocalSchematicModel(
            path,
            standaloneLocalSchematicModel -> new HierarchicalLocalSchematicModel(
                new HierarchicalSchematicModel<>(standaloneLocalSchematicModel)
            )
        );
    }

    private HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel(
        Path path,
        Function<StandaloneLocalSchematicModel, HierarchicalLocalSchematicModel> combiner
    ) {
        var hierarchicalLocalSchematicModel = combiner.apply(standaloneLocalSchematicModel(path));
        if (hasLocalTemplate(hierarchicalLocalSchematicModel)) {
            return hierarchicalLocalSchematicModel(
                hierarchicalLocalSchematicModel.templatePath(),
                hierarchicalLocalSchematicModel::inheritedFrom
            );
        }
        return hierarchicalLocalSchematicModel;
    }

    private boolean hasLocalTemplate(HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel) {
        return hierarchicalLocalSchematicModel.template() instanceof SchematicTemplateModel &&
               Files.exists(hierarchicalLocalSchematicModel.templatePath());
    }
}
