package com.github.maximtereshchenko.conveyor.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class Plugin extends StoredArtifact<ArtifactDependencyModel> {

    private static final String ENABLED_CONFIGURATION_KEY = "enabled";

    private final PluginModel pluginModel;
    private final Properties properties;
    private final ModelFactory modelFactory;
    private final Preferences preferences;

    Plugin(
        PluginModel pluginModel,
        Properties properties,
        ModelFactory modelFactory,
        Preferences preferences,
        Repositories repositories
    ) {
        super(repositories);
        this.pluginModel = pluginModel;
        this.properties = properties;
        this.modelFactory = modelFactory;
        this.preferences = preferences;
    }

    @Override
    public String name() {
        return pluginModel.name();
    }

    @Override
    public SemanticVersion version() {
        return pluginModel.version()
            .map(properties::interpolated)
            .map(SemanticVersion::new)
            .or(() -> preferences.version(pluginModel.name()))
            .orElseThrow();
    }

    @Override
    Set<ArtifactDependencyModel> dependencyModels() {
        return modelFactory.manualHierarchy(pluginModel.name(), version(), repositories()).dependencies();
    }

    @Override
    Dependency dependency(ArtifactDependencyModel dependencyModel) {
        return new TransitiveDependency(dependencyModel, modelFactory, properties, preferences, repositories());
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
