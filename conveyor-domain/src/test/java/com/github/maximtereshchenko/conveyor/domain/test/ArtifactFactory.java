package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;

final class ArtifactFactory {

    private final GsonAdapter gsonAdapter;

    ArtifactFactory(GsonAdapter gsonAdapter) {
        this.gsonAdapter = gsonAdapter;
    }

    ConveyorPluginBuilder plugin() {
        return ConveyorPluginBuilder.empty(gsonAdapter);
    }

    DependencyBuilder dependency() {
        return DependencyBuilder.empty(gsonAdapter);
    }

    ProjectDefinitionBuilder project(String name) {
        return ProjectDefinitionBuilder.empty(gsonAdapter, name);
    }

    ProjectDefinitionBuilder conveyorJson() {
        return ProjectDefinitionBuilder.conveyorJson(gsonAdapter);
    }

    ProjectDefinitionBuilder superParent() {
        return ProjectDefinitionBuilder.superParent(gsonAdapter);
    }
}
