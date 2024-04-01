package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import java.util.Collection;
import java.util.List;

final class SuperParent implements Parent {

    private final Collection<PluginDefinition> plugins;

    SuperParent(Collection<PluginDefinition> plugins) {
        this.plugins = List.copyOf(plugins);
    }

    static Parent from(DirectoryRepository repository) {
        return new SuperParent(repository.projectDefinition(new SuperParentArtifactDefinition()).plugins());
    }

    @Override
    public Collection<PluginDefinition> plugins() {
        return plugins;
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
