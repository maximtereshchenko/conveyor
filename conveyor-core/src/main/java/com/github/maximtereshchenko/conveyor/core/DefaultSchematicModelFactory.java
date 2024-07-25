package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

final class DefaultSchematicModelFactory implements SchematicModelFactory {

    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final Tracer tracer;

    DefaultSchematicModelFactory(
        SchematicDefinitionConverter schematicDefinitionConverter,
        Tracer tracer
    ) {
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.tracer = tracer;
    }

    @Override
    public LinkedHashSet<ExtendableLocalInheritanceHierarchyModel> extendableLocalInheritanceHierarchyModels(
        Path path
    ) {
        var allSchematicPaths = extendableLocalInheritanceHierarchyModel(path)
            .map(ExtendableLocalInheritanceHierarchyModel::rootPath)
            .map(this::allSchematicPaths)
            .orElse(List.of());
        var models = new LinkedHashSet<ExtendableLocalInheritanceHierarchyModel>();
        for (var schematicPath : allSchematicPaths) {
            var model = extendableLocalInheritanceHierarchyModel(schematicPath);
            model.ifPresent(tracer::submitLocalModel);
            model.ifPresent(models::add);
        }
        return models;
    }

    @Override
    public CompleteInheritanceHierarchyModel completeInheritanceHierarchyModel(
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

    @Override
    public InheritanceHierarchyModel<SchematicModel> inheritanceHierarchyModel(
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

    private Optional<StandaloneLocalSchematicModel> standaloneLocalSchematicModel(Path path) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        return Optional.of(
            new StandaloneLocalSchematicModel(
                path,
                new StandaloneSchematicModel(schematicDefinitionConverter.schematicDefinition(path))
            )
        );
    }

    private List<Path> allSchematicPaths(Path path) {
        return Stream.concat(
                Stream.of(path),
                standaloneLocalSchematicModel(path)
                    .map(StandaloneLocalSchematicModel::inclusions)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(this::allSchematicPaths)
                    .flatMap(Collection::stream)
            )
            .toList();
    }

    private Optional<ExtendableLocalInheritanceHierarchyModel> extendableLocalInheritanceHierarchyModel(
        Path path
    ) {
        return standaloneLocalSchematicModel(path)
            .map(standaloneLocalSchematicModel ->
                new InheritanceHierarchyModel<LocalSchematicModel>(standaloneLocalSchematicModel)
            )
            .map(ExtendableLocalInheritanceHierarchyModel::new)
            .map(this::extendableLocalInheritanceHierarchyModel);
    }

    private ExtendableLocalInheritanceHierarchyModel extendableLocalInheritanceHierarchyModel(
        ExtendableLocalInheritanceHierarchyModel localModel
    ) {
        if (!(localModel.template() instanceof SchematicTemplateModel schematicTemplateModel)) {
            return localModel;
        }
        return standaloneLocalSchematicModel(localModel.templatePath())
            .filter(standaloneLocalSchematicModel ->
                schematicTemplateModel.id().equals(standaloneLocalSchematicModel.id()) &&
                schematicTemplateModel.version().equals(standaloneLocalSchematicModel.version())
            )
            .map(localModel::inheritedFrom)
            .map(this::extendableLocalInheritanceHierarchyModel)
            .orElse(localModel);
    }
}
