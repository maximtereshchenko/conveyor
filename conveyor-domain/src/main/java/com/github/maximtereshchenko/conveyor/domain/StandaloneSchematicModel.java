package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.NoTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinition;
import com.github.maximtereshchenko.conveyor.api.port.SchematicTemplateDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

record StandaloneSchematicModel(SchematicDefinition schematicDefinition) implements SchematicModel {

    @Override
    public String group() {
        return schematicDefinition.group();
    }

    @Override
    public String name() {
        return schematicDefinition.name();
    }

    @Override
    public SemanticVersion version() {
        return new SemanticVersion(schematicDefinition.version());
    }

    @Override
    public TemplateModel template() {
        return switch (schematicDefinition.template()) {
            case SchematicTemplateDefinition definition -> new SchematicTemplateModel(
                definition.group(),
                definition.name(),
                new SemanticVersion(definition.version())
            );
            case NoTemplateDefinition ignored -> new NoTemplateModel();
        };
    }

    @Override
    public Map<String, String> properties() {
        var properties = new HashMap<>(schematicDefinition.properties());
        properties.put(SchematicPropertyKey.SCHEMATIC_NAME.fullName(), schematicDefinition.name());
        properties.put(
            SchematicPropertyKey.SCHEMATIC_VERSION.fullName(),
            schematicDefinition.version()
        );
        properties.putIfAbsent(
            SchematicPropertyKey.TEMPLATE_LOCATION.fullName(),
            "../conveyor.json"
        );
        return properties;
    }

    @Override
    public PreferencesModel preferences() {
        return new PreferencesModel(
            schematicDefinition.preferences()
                .inclusions()
                .stream()
                .map(definition ->
                    new PreferencesInclusionModel(
                        definition.group(),
                        definition.name(),
                        definition.version()
                    )
                )
                .collect(Collectors.toSet()),
            schematicDefinition.preferences()
                .artifacts()
                .stream()
                .map(definition ->
                    new ArtifactPreferenceModel(
                        definition.group(),
                        definition.name(),
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
                    pluginDefinition.group(),
                    pluginDefinition.name(),
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
                    dependencyDefinition.group(),
                    dependencyDefinition.name(),
                    dependencyDefinition.version(),
                    dependencyDefinition.scope()
                )
            )
            .collect(Collectors.toSet());
    }
}
