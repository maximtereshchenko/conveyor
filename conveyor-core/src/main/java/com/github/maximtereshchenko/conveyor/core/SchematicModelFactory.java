package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SchematicModelFactory {

    private final SchematicDefinitionConverter schematicDefinitionConverter;

    SchematicModelFactory(SchematicDefinitionConverter schematicDefinitionConverter) {
        this.schematicDefinitionConverter = schematicDefinitionConverter;
    }

    LinkedHashSet<ExtendableLocalInheritanceHierarchyModel> extendableLocalInheritanceHierarchyModels(
        Path path
    ) {
        return allSchematicPaths(extendableLocalInheritanceHierarchyModel(path).rootPath())
            .map(this::extendableLocalInheritanceHierarchyModel)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    CompleteInheritanceHierarchyModel completeInheritanceHierarchyModel(
        ExtendableLocalInheritanceHierarchyModel localModel,
        Repositories repositories
    ) {
        return switch (localModel.template()) {
            case NoTemplateModel ignored -> new CompleteInheritanceHierarchyModel(localModel);
            case SchematicTemplateModel model -> new CompleteInheritanceHierarchyModel(
                inheritanceHierarchyModel(model.id(), model.version(), repositories),
                localModel
            );
        };
    }

    InheritanceHierarchyModel<SchematicModel> inheritanceHierarchyModel(
        Id id,
        Version version,
        Repositories repositories
    ) {
        return inheritanceHierarchyModel(
            id,
            version,
            repositories,
            InheritanceHierarchyModel<SchematicModel>::new
        );
    }

    private InheritanceHierarchyModel<SchematicModel> inheritanceHierarchyModel(
        Id id,
        Version version,
        Repositories repositories,
        Function<StandaloneSchematicModel, InheritanceHierarchyModel<SchematicModel>> combiner
    ) {
        var inheritanceHierarchyModel = combiner.apply(
            new StandaloneSchematicModel(repositories.schematicDefinition(id, version))
        );
        return switch (inheritanceHierarchyModel.template()) {
            case NoTemplateModel ignored -> inheritanceHierarchyModel;
            case SchematicTemplateModel model -> inheritanceHierarchyModel(
                model.id(),
                model.version(),
                repositories,
                inheritanceHierarchyModel::inheritedFrom
            );
        };
    }

    private StandaloneLocalSchematicModel standaloneLocalSchematicModel(Path path) {
        return new StandaloneLocalSchematicModel(
            path,
            new StandaloneSchematicModel(schematicDefinitionConverter.schematicDefinition(path))
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

    private ExtendableLocalInheritanceHierarchyModel extendableLocalInheritanceHierarchyModel(
        Path path
    ) {
        return extendableLocalInheritanceHierarchyModel(
            path,
            standaloneLocalSchematicModel -> new ExtendableLocalInheritanceHierarchyModel(
                new InheritanceHierarchyModel<>(standaloneLocalSchematicModel)
            )
        );
    }

    private ExtendableLocalInheritanceHierarchyModel extendableLocalInheritanceHierarchyModel(
        Path path,
        Function<StandaloneLocalSchematicModel, ExtendableLocalInheritanceHierarchyModel> combiner
    ) {
        var extendableLocalInheritanceHierarchyModel = combiner.apply(
            standaloneLocalSchematicModel(path)
        );
        if (hasLocalTemplate(extendableLocalInheritanceHierarchyModel)) {
            return extendableLocalInheritanceHierarchyModel(
                extendableLocalInheritanceHierarchyModel.templatePath(),
                extendableLocalInheritanceHierarchyModel::inheritedFrom
            );
        }
        return extendableLocalInheritanceHierarchyModel;
    }

    private boolean hasLocalTemplate(ExtendableLocalInheritanceHierarchyModel localModel) {
        return localModel.template() instanceof SchematicTemplateModel &&
               Files.exists(localModel.templatePath());
    }
}
