package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Products;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

final class Schematic extends Definition {

    private final Template template;
    private final SchematicDefinition schematicDefinition;
    private final Path path;

    private Schematic(
        DefinitionReader definitionReader,
        Template template,
        SchematicDefinition schematicDefinition,
        Path path
    ) {
        super(definitionReader);
        this.template = template;
        this.schematicDefinition = schematicDefinition;
        this.path = path;
    }

    static Schematic from(DefinitionReader definitionReader, Path path) {
        return from(
            definitionReader,
            templateDefinition -> switch (templateDefinition) {
                case ManualTemplateDefinition definition ->
                    new Manual(definitionReader, definition.name(), definition.version());
                case NoExplicitTemplate ignored -> Manual.superManual(definitionReader);
            },
            path
        );
    }

    static Schematic from(DefinitionReader definitionReader, Template template, Path path) {
        return from(definitionReader, templateDefinition -> template, path);
    }

    private static Schematic from(
        DefinitionReader definitionReader,
        Function<TemplateDefinition, Template> templateFunction,
        Path path
    ) {
        var schematicDefinition = definitionReader.schematicDefinition(path);
        return new Schematic(
            definitionReader,
            templateFunction.apply(schematicDefinition.template()),
            schematicDefinition,
            path
        );
    }

    @Override
    public Optional<Repository> repository() {
        return schematicDefinition.repository()
            .map(path -> new Repository(path, definitionReader()))
            .or(template::repository);
    }

    @Override
    public Properties properties(Repository repository) {
        return properties(schematicDefinition.properties())
            .override(template.properties(repository));
    }

    @Override
    public Plugins plugins(Repository repository) {
        return plugins(schematicDefinition.plugins())
            .override(template.plugins(repository));
    }

    @Override
    public Dependencies dependencies(Repository repository, SchematicProducts schematicProducts) {
        return dependencies(schematicDefinition.dependencies(), schematicProducts)
            .override(template.dependencies(repository, schematicProducts));
    }

    ImmutableList<Schematic> inclusions() {
        return schematicDefinition.inclusions()
            .stream()
            .map(path -> Schematic.from(definitionReader(), this, path))
            .collect(new ImmutableListCollector<>());
    }

    boolean dependsOn(Schematic schematic) {
        return template.equals(schematic) || hasSchematicDependency(schematic.schematicDefinition.name());
    }

    SchematicProducts construct(Repository repository, SchematicProducts schematicProducts, Stage stage) {
        return schematicProducts.with(
            schematicDefinition.name(),
            plugins(repository)
                .executeTasks(
                    new Products().with(path, ProductType.SCHEMATIC_DEFINITION),
                    repository,
                    properties(repository).withDefaults(schematicDefinition.name(), path),
                    dependencies(repository, schematicProducts),
                    stage
                )
        );
    }

    private boolean hasSchematicDependency(String name) {
        return schematicDefinition.dependencies()
            .stream()
            .anyMatch(dependencyDefinition ->
                switch (dependencyDefinition) {
                    case ArtifactDependencyDefinition ignored -> false;
                    case SchematicDependencyDefinition definition -> definition.schematic().equals(name);
                }
            );
    }
}
