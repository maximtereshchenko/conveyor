package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;

final class ArtifactFactory {

    private final GsonAdapter gsonAdapter;

    ArtifactFactory(GsonAdapter gsonAdapter) {
        this.gsonAdapter = gsonAdapter;
    }

    ConveyorPluginBuilder pluginBuilder() {
        return ConveyorPluginBuilder.empty(gsonAdapter);
    }

    DependencyBuilder dependencyBuilder() {
        return DependencyBuilder.empty(gsonAdapter);
    }

    ProjectDefinitionBuilder projectBuilder(String name) {
        return ProjectDefinitionBuilder.empty(gsonAdapter, name);
    }

    ProjectDefinitionBuilder conveyorJson() {
        return ProjectDefinitionBuilder.conveyorJson(gsonAdapter);
    }

    ProjectDefinitionBuilder superManual() {
        return ProjectDefinitionBuilder.superManual(gsonAdapter);
    }
}
