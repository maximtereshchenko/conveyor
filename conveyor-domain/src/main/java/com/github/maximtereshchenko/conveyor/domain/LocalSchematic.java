package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;

final class LocalSchematic implements Schematic {

    private final Manual template;
    private final SchematicDefinition schematicDefinition;
    private final Repository repository;
    private final Path path;
    private final DefinitionReader definitionReader;

    LocalSchematic(
        Manual template,
        SchematicDefinition schematicDefinition,
        Repository repository,
        Path path,
        DefinitionReader definitionReader
    ) {
        this.template = template;
        this.schematicDefinition = schematicDefinition;
        this.repository = repository;
        this.path = path;
        this.definitionReader = definitionReader;
    }

    @Override
    public String name() {
        return schematicDefinition.name();
    }

    @Override
    public SchematicProducts construct(SchematicProducts schematicProducts, Stage stage) {
        return schematicProducts.with(
            schematicDefinition.name(),
            plugins().conveyorPlugins(
                    properties().conveyorProperties(path, schematicDefinition.name()),
                    dependencies(schematicProducts)
                )
                .executeTasks(stage)
        );
    }

    @Override
    public ImmutableList<Schematic> inclusions() {
        return schematicDefinition.inclusions()
            .stream()
            .map(path -> new LocalSchematic(this,
                definitionReader.schematicDefinition(path),
                repository,
                path,
                definitionReader))
            .collect(new ImmutableListCollector<>());
    }

    @Override
    public boolean dependsOn(Schematic schematic) {
        return schematicDefinition.dependencies()
            .stream()
            .anyMatch(dependencyDefinition ->
                switch (dependencyDefinition) {
                    case ArtifactDependencyDefinition definition -> definition.name().equals(schematic.name());
                    case SchematicDependencyDefinition definition -> definition.schematic().equals(schematic.name());
                }
            );
    }

    @Override
    public Properties properties() {
        return template.properties().override(new Properties(schematicDefinition.properties()));
    }

    @Override
    public Plugins plugins() {
        return template.plugins()
            .override(
                Plugins.from(
                    schematicDefinition.plugins()
                        .stream()
                        .map(dependency -> new DefinedPlugin(dependency, repository, properties()))
                        .collect(new ImmutableSetCollector<>())
                )
            );
    }

    @Override
    public Dependencies dependencies(SchematicProducts schematicProducts) {
        return template.dependencies(schematicProducts)
            .with(
                Dependencies.from(
                    schematicDefinition.dependencies()
                        .stream()
                        .map(definition -> dependency(definition, schematicProducts))
                        .collect(new ImmutableSetCollector<>())
                )
            );
    }

    private Dependency dependency(DependencyDefinition dependencyDefinition, SchematicProducts schematicProducts) {
        return switch (dependencyDefinition) {
            case ArtifactDependencyDefinition definition -> new DefinedDependency(definition, repository);
            case SchematicDependencyDefinition definition ->
                new SchematicProductsDependency(definition, schematicProducts);
        };
    }
}
