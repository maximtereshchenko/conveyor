package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.gson.JacksonAdapter;

final class BuilderFactory {

    private final JacksonAdapter gsonAdapter;

    BuilderFactory(JacksonAdapter gsonAdapter) {
        this.gsonAdapter = gsonAdapter;
    }

    SchematicBuilder schematicBuilder() {
        return new SchematicBuilder(gsonAdapter).name("project"); //TODO
    }

    RepositoryBuilder repositoryBuilder() {
        return new RepositoryBuilder(gsonAdapter);
    }

    ManualBuilder manualBuilder() {
        return new ManualBuilder(gsonAdapter);
    }
}
