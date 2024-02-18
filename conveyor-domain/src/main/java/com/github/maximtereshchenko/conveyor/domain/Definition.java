package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.*;

import java.util.Collection;
import java.util.Map;

abstract class Definition implements Template {

    private final DefinitionReader definitionReader;

    Definition(DefinitionReader definitionReader) {
        this.definitionReader = definitionReader;
    }

    DefinitionReader definitionReader() {
        return definitionReader;
    }

    Properties properties(Map<String, String> raw) {
        return new Properties(new ImmutableMap<>(raw));
    }

    Plugins plugins(Collection<PluginDefinition> pluginDefinitions) {
        return Plugins.from(
            pluginDefinitions.stream()
                .map(this::plugin)
                .collect(new ImmutableSetCollector<>())
        );
    }

    Dependencies dependencies(
        Collection<? extends DependencyDefinition> dependencyDefinitions,
        SchematicProducts schematicProducts
    ) {
        return Dependencies.from(
            dependencyDefinitions.stream()
                .map(dependencyDefinition -> dependency(dependencyDefinition, schematicProducts))
                .collect(new ImmutableSetCollector<>())
        );
    }

    private Plugin plugin(PluginDefinition pluginDefinition) {
        var configuration = Configuration.from(pluginDefinition.configuration());
        if (pluginDefinition.version().isEmpty()) {
            return new PluginWithoutVersion(pluginDefinition.name(), configuration);
        }
        return new VersionedPlugin(pluginDefinition.name(), pluginDefinition.version().getAsInt(), configuration);
    }

    private Dependency dependency(DependencyDefinition dependencyDefinition, SchematicProducts schematicProducts) {
        return switch (dependencyDefinition) {
            case ArtifactDependencyDefinition definition -> new PackagedDependency(definition);
            case SchematicDependencyDefinition definition ->
                new SchematicDependency(definition, schematicProducts, definitionReader);
        };
    }
}
