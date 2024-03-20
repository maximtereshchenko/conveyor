package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;

import java.nio.file.Path;
import java.util.Set;

final class SchematicDependency extends DependentArtifact<DependencyModel> implements Dependency {

    private final SchematicDependencyModel schematicDependencyModel;
    private final SchematicProducts schematicProducts;
    private final ModelFactory modelFactory;
    private final Repositories repositories;
    private final Preferences preferences;

    SchematicDependency(
        SchematicDependencyModel schematicDependencyModel,
        SchematicProducts schematicProducts,
        ModelFactory modelFactory,
        Repositories repositories,
        Preferences preferences
    ) {
        this.schematicDependencyModel = schematicDependencyModel;
        this.schematicProducts = schematicProducts;
        this.modelFactory = modelFactory;
        this.repositories = repositories;
        this.preferences = preferences;
    }

    @Override
    public String name() {
        return schematicDependencyModel.name();
    }

    @Override
    public SemanticVersion version() {
        return fullSchematicHierarchy().version();
    }

    @Override
    public Path path() {
        return product(ProductType.MODULE);
    }

    @Override
    public DependencyScope scope() {
        return schematicDependencyModel.scope().orElse(DependencyScope.IMPLEMENTATION);
    }

    @Override
    Set<DependencyModel> dependencyModels() {
        return fullSchematicHierarchy().dependencies();
    }

    @Override
    Dependency dependency(DependencyModel dependencyModel) {
        return switch (dependencyModel) {
            case ArtifactDependencyModel model ->
                new TransitiveDependency(model, modelFactory, preferences, repositories);
            case SchematicDependencyModel model ->
                new SchematicDependency(model, schematicProducts, modelFactory, repositories, preferences);
        };
    }

    private FullSchematicHierarchy fullSchematicHierarchy() {
        return modelFactory.fullSchematicHierarchy(product(ProductType.SCHEMATIC_DEFINITION), repositories);
    }

    private Path product(ProductType productType) {
        return schematicProducts.byType(schematicDependencyModel.name(), productType).iterator().next();
    }
}
