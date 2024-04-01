package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PreferencesDefinition;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

abstract class BaseModel<T extends TemplateModel, M extends DependencyModel, D> implements Model<T, M> {

    @Override
    public PreferencesModel preferences() {
        return new PreferencesModel(
            preferencesDefinition()
                .inclusions()
                .stream()
                .map(definition ->
                    new PreferencesInclusionModel(definition.name(), new SemanticVersion(definition.version()))
                )
                .collect(Collectors.toSet()),
            preferencesDefinition()
                .artifacts()
                .stream()
                .map(definition ->
                    new ArtifactPreferenceModel(definition.name(), new SemanticVersion(definition.version()))
                )
                .collect(Collectors.toSet())
        );
    }

    @Override
    public Set<PluginModel> plugins() {
        return pluginDefinitions()
            .stream()
            .map(pluginDefinition ->
                new PluginModel(
                    pluginDefinition.name(),
                    pluginDefinition.version()
                        .map(SemanticVersion::new),
                    pluginDefinition.configuration()
                )
            )
            .collect(Collectors.toSet());
    }

    @Override
    public Set<M> dependencies() {
        return dependencyDefinitions()
            .stream()
            .map(this::dependencyModel)
            .collect(Collectors.toSet());
    }

    abstract PreferencesDefinition preferencesDefinition();

    abstract Collection<PluginDefinition> pluginDefinitions();

    abstract Collection<D> dependencyDefinitions();

    abstract M dependencyModel(D dependencyDefinition);
}
