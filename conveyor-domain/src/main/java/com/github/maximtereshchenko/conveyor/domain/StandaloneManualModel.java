package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

final class StandaloneManualModel
    extends BaseModel<ManualTemplateModel, ArtifactDependencyModel, ManualDependencyDefinition> {

    private final ManualDefinition manualDefinition;

    StandaloneManualModel(ManualDefinition manualDefinition) {
        this.manualDefinition = manualDefinition;
    }

    @Override
    public String name() {
        return manualDefinition.name();
    }

    @Override
    public SemanticVersion version() {
        return new SemanticVersion(manualDefinition.version());
    }

    @Override
    public ManualTemplateModel template() {
        return switch (manualDefinition.template()) {
            case ManualTemplateDefinition definition ->
                new OtherManualTemplateModel(definition.name(), new SemanticVersion(definition.version()));
            case NoExplicitlyDefinedTemplate ignored -> new NoTemplateModel();
        };
    }

    @Override
    public Map<String, String> properties() {
        return manualDefinition.properties();
    }

    @Override
    PreferencesDefinition preferencesDefinition() {
        return manualDefinition.preferences();
    }

    @Override
    Collection<PluginDefinition> pluginDefinitions() {
        return manualDefinition.plugins();
    }

    @Override
    Collection<ManualDependencyDefinition> dependencyDefinitions() {
        return manualDefinition.dependencies();
    }

    @Override
    ArtifactDependencyModel dependencyModel(ManualDependencyDefinition dependencyDefinition) {
        return new ArtifactDependencyModel(
            dependencyDefinition.name(),
            Optional.of(dependencyDefinition.version()),
            Optional.of(dependencyDefinition.scope())
        );
    }
}
