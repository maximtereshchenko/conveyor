package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.NoTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicTemplateDefinition;

import java.util.Set;
import java.util.stream.Collectors;

record StandaloneSchematicModel(SchematicDefinition schematicDefinition) implements SchematicModel {

    @Override
    public Id id() {
        return new Id(schematicDefinition.group(), schematicDefinition().name());
    }

    @Override
    public SemanticVersion version() {
        return new SemanticVersion(schematicDefinition.version());
    }

    @Override
    public TemplateModel template() {
        return switch (schematicDefinition.template()) {
            case SchematicTemplateDefinition definition -> new SchematicTemplateModel(
                new Id(definition.group(), definition.name()),
                new SemanticVersion(definition.version())
            );
            case NoTemplateDefinition ignored -> new NoTemplateModel();
        };
    }

    @Override
    public PropertiesModel properties() {
        return PropertiesModel.from(schematicDefinition.properties())
            .with(SchematicPropertyKey.SCHEMATIC_NAME, schematicDefinition.name())
            .with(SchematicPropertyKey.SCHEMATIC_VERSION, schematicDefinition.version());
    }

    @Override
    public PreferencesModel preferences() {
        return new PreferencesModel(
            schematicDefinition.preferences()
                .inclusions()
                .stream()
                .map(definition ->
                    new PreferencesInclusionModel(
                        new Id(definition.group(), definition.name()),
                        definition.version()
                    )
                )
                .collect(Collectors.toSet()),
            schematicDefinition.preferences()
                .artifacts()
                .stream()
                .map(definition ->
                    new ArtifactPreferenceModel(
                        new Id(definition.group(), definition.name()),
                        definition.version()
                    )
                )
                .collect(Collectors.toSet())
        );
    }

    @Override
    public Set<PluginModel> plugins() {
        return schematicDefinition.plugins()
            .stream()
            .map(pluginDefinition ->
                new PluginModel(
                    new Id(pluginDefinition.group(), pluginDefinition.name()),
                    pluginDefinition.version(),
                    pluginDefinition.configuration()
                )
            )
            .collect(Collectors.toSet());
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return schematicDefinition.dependencies()
            .stream()
            .map(dependencyDefinition ->
                new DependencyModel(
                    new Id(dependencyDefinition.group(), dependencyDefinition.name()),
                    dependencyDefinition.version(),
                    dependencyDefinition.scope()
                )
            )
            .collect(Collectors.toSet());
    }
}
