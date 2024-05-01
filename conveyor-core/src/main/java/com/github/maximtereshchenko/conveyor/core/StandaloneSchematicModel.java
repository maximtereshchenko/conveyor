package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.NoTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicTemplateDefinition;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

record StandaloneSchematicModel(SchematicDefinition schematicDefinition) implements SchematicModel {

    @Override
    public Id id() {
        return new Id(
            schematicDefinition.group()
                .or(() -> fromSchematicTemplateDefinition(SchematicTemplateDefinition::group))
                .orElseThrow(),
            schematicDefinition().name()
        );
    }

    @Override
    public Version version() {
        return schematicDefinition.version()
            .or(() -> fromSchematicTemplateDefinition(SchematicTemplateDefinition::version))
            .map(Version::new)
            .orElseThrow();
    }

    @Override
    public TemplateModel template() {
        return switch (schematicDefinition.template()) {
            case SchematicTemplateDefinition definition -> new SchematicTemplateModel(
                new Id(definition.group(), definition.name()),
                new Version(definition.version())
            );
            case NoTemplateDefinition ignored -> new NoTemplateModel();
        };
    }

    @Override
    public PropertiesModel properties() {
        return PropertiesModel.from(schematicDefinition.properties())
            .with(SchematicPropertyKey.SCHEMATIC_GROUP, id().group())
            .with(SchematicPropertyKey.SCHEMATIC_NAME, schematicDefinition.name())
            .with(SchematicPropertyKey.SCHEMATIC_VERSION, version().toString());
    }

    @Override
    public PreferencesModel preferences() {
        return new PreferencesModel(
            schematicDefinition.preferences()
                .inclusions()
                .stream()
                .map(definition ->
                    new PreferencesInclusionModel(
                        new IdModel(definition.group(), definition.name()),
                        definition.version()
                    )
                )
                .collect(Collectors.toCollection(LinkedHashSet::new)),
            schematicDefinition.preferences()
                .artifacts()
                .stream()
                .map(definition ->
                    new ArtifactPreferenceModel(
                        new IdModel(definition.group(), definition.name()),
                        definition.version()
                    )
                )
                .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

    @Override
    public LinkedHashSet<PluginModel> plugins() {
        return schematicDefinition.plugins()
            .stream()
            .map(pluginDefinition ->
                new PluginModel(
                    new IdModel(pluginDefinition.group(), pluginDefinition.name()),
                    pluginDefinition.version(),
                    pluginDefinition.configuration()
                )
            )
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return schematicDefinition.dependencies()
            .stream()
            .map(dependencyDefinition ->
                new DependencyModel(
                    new IdModel(dependencyDefinition.group(), dependencyDefinition.name()),
                    dependencyDefinition.version(),
                    dependencyDefinition.scope(),
                    exclusions(dependencyDefinition)
                )
            )
            .collect(Collectors.toSet());
    }

    private Optional<String> fromSchematicTemplateDefinition(
        Function<SchematicTemplateDefinition, String> mapper
    ) {
        return switch (schematicDefinition.template()) {
            case SchematicTemplateDefinition definition -> Optional.of(mapper.apply(definition));
            case NoTemplateDefinition ignored -> Optional.empty();
        };
    }

    private Set<Id> exclusions(DependencyDefinition dependencyDefinition) {
        return dependencyDefinition.exclusions()
            .stream()
            .map(exclusionDefinition ->
                new Id(exclusionDefinition.group(), exclusionDefinition.name())
            )
            .collect(Collectors.toSet());
    }
}
