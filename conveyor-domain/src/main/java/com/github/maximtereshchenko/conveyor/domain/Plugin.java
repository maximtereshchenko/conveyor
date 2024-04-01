package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class Plugin implements Artifact {

    private static final String ENABLED_CONFIGURATION_KEY = "enabled";

    private final DirectlyReferencedArtifact directlyReferencedArtifact;
    private final PluginModel pluginModel;
    private final Properties properties;

    Plugin(
        DirectlyReferencedArtifact directlyReferencedArtifact,
        PluginModel pluginModel,
        Properties properties
    ) {
        this.directlyReferencedArtifact = directlyReferencedArtifact;
        this.pluginModel = pluginModel;
        this.properties = properties;
    }

    @Override
    public String group() {
        return directlyReferencedArtifact.group();
    }

    @Override
    public String name() {
        return directlyReferencedArtifact.name();
    }

    @Override
    public SemanticVersion semanticVersion() {
        return directlyReferencedArtifact.semanticVersion();
    }

    @Override
    public Path path() {
        return directlyReferencedArtifact.path();
    }

    @Override
    public Set<Artifact> dependencies() {
        return directlyReferencedArtifact.dependencies();
    }

    boolean isEnabled() {
        return Boolean.parseBoolean(configuration().get(ENABLED_CONFIGURATION_KEY));
    }

    Map<String, String> configuration() {
        var configuration = new HashMap<String, String>();
        for (var entry : pluginModel.configuration().entrySet()) {
            if (entry.getValue().isBlank()) {
                continue;
            }
            configuration.put(entry.getKey(), properties.interpolated(entry.getValue()));
        }
        configuration.putIfAbsent(ENABLED_CONFIGURATION_KEY, "true");
        return configuration;
    }
}
