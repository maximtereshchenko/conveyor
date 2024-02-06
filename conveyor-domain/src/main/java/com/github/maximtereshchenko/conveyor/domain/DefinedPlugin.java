package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;

import java.nio.file.Path;

final class DefinedPlugin implements Plugin {

    private final Artifact artifact;
    private final PluginDefinition pluginDefinition;
    private final Properties properties;

    DefinedPlugin(PluginDefinition pluginDefinition, Repository repository, Properties properties) {
        this.artifact = new DefinedArtifact(pluginDefinition, repository);
        this.pluginDefinition = pluginDefinition;
        this.properties = properties;
    }

    @Override
    public String name() {
        return artifact.name();
    }

    @Override
    public int version() {
        return artifact.version();
    }

    @Override
    public Dependencies dependencies() {
        return artifact.dependencies();
    }

    @Override
    public Path modulePath() {
        return artifact.modulePath();
    }

    @Override
    public Configuration configuration() {
        return new Configuration(
            properties,
            new ImmutableMap<>(pluginDefinition.configuration())
        );
    }
}
