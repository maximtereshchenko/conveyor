package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

final class SchematicModelFactory {

    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final Tracer tracer;

    SchematicModelFactory(
        SchematicDefinitionConverter schematicDefinitionConverter,
        Tracer tracer
    ) {
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.tracer = tracer;
    }

    LinkedHashSet<ExtendableLocalInheritanceHierarchyModel> extendableLocalInheritanceHierarchyModels(
        Path path
    ) {
        var allSchematicPaths = allSchematicPaths(
            extendableLocalInheritanceHierarchyModel(path).rootPath()
        );
        var models = new LinkedHashSet<ExtendableLocalInheritanceHierarchyModel>();
        for (var schematicPath : allSchematicPaths) {
            var model = extendableLocalInheritanceHierarchyModel(schematicPath);
            tracer.submitLocalModel(model);
            models.add(model);
        }
        return models;
    }

    CompleteInheritanceHierarchyModel completeInheritanceHierarchyModel(
        ExtendableLocalInheritanceHierarchyModel localModel,
        Repositories repositories
    ) {
        var completeModel = switch (localModel.template()) {
            case NoTemplateModel ignored -> new CompleteInheritanceHierarchyModel(localModel);
            case SchematicTemplateModel model -> new CompleteInheritanceHierarchyModel(
                inheritanceHierarchyModel(model.id(), model.version(), repositories),
                localModel
            );
        };
        tracer.submitCompleteModel(completeModel);
        return completeModel;
    }

    InheritanceHierarchyModel<SchematicModel> inheritanceHierarchyModel(
        Id id,
        Version version,
        Repositories repositories
    ) {
        var model = inheritanceHierarchyModel(
            id,
            version,
            repositories,
            InheritanceHierarchyModel<SchematicModel>::new
        );
        tracer.submitModel(model);
        return model;
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

    private List<Path> allSchematicPaths(Path path) {
        return Stream.concat(
                Stream.of(path),
                standaloneLocalSchematicModel(path)
                    .inclusions()
                    .stream()
                    .map(this::allSchematicPaths)
                    .flatMap(Collection::stream)
            )
            .toList();
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
