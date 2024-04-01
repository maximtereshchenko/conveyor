package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;

final class BuilderFactory {

    private final GsonAdapter gsonAdapter;

    BuilderFactory(GsonAdapter gsonAdapter) {
        this.gsonAdapter = gsonAdapter;
    }

    SchematicBuilder schematicBuilder() {
        return new SchematicBuilder(gsonAdapter).name("project");
    }

    RepositoryBuilder repositoryBuilder() {
        return new RepositoryBuilder(gsonAdapter);
    }
}
