package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import java.util.Collection;
import java.util.Map;

final class SuperParent implements Parent {

    private final ProjectDefinition projectDefinition;

    SuperParent(ProjectDefinition projectDefinition) {
        this.projectDefinition = projectDefinition;
    }

    static Parent from(DirectoryRepository repository) {
        return new SuperParent(repository.projectDefinition(new SuperParentArtifactDefinition()));
    }

    @Override
    public Collection<PluginDefinition> plugins() {
        return projectDefinition.plugins();
    }

    @Override
    public Collection<DependencyDefinition> dependencies() {
        return projectDefinition.dependencies();
    }

    @Override
    public Map<String, String> properties() {
        return projectDefinition.properties();
    }

    private static final class SuperParentArtifactDefinition implements ArtifactDefinition {

        @Override
        public String name() {
            return "super-parent";
        }

        @Override
        public int version() {
            return 1;
        }
    }
}
