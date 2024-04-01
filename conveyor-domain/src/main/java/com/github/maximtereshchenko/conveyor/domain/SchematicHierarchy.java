package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

abstract class SchematicHierarchy<T extends TemplateModel, R extends TemplateModel>
    extends Hierarchy<T, DependencyModel, SchematicModel<? extends R>>
    implements SchematicModel<T> {

    SchematicHierarchy(LinkedHashSet<SchematicModel<? extends R>> models) {
        super(models);
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return reduce(Model::dependencies, DependencyModel::name, DependencyModel::override);
    }

    @Override
    public Path path() {
        return models().getLast().path();
    }

    @Override
    public Set<RepositoryModel> repositories() {
        return reduce(
            SchematicModel::repositories,
            RepositoryModel::name,
            RepositoryModel::override
        );
    }
}

