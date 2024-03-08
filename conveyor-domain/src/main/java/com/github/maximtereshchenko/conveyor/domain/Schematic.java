package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Products;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class Schematic extends Definition {

    private final Template template;
    private final SchematicDefinition schematicDefinition;
    private final Path path;

    Schematic(
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
                case SchematicPathTemplateDefinition definition -> from(definitionReader, definition.path());
                case NoExplicitlyDefinedTemplate ignored -> defaultTemplate(definitionReader, path);
            },
            path
        );
    }

    static Schematic from(DefinitionReader definitionReader, Template template, Path path) {
        return from(definitionReader, templateDefinition -> template, path);
    }

    private static Template defaultTemplate(DefinitionReader definitionReader, Path path) {
        var defaultSchematicTemplatePath = path.getParent().getParent().resolve("conveyor.json");
        if (Files.exists(defaultSchematicTemplatePath)) {
            return from(definitionReader, defaultSchematicTemplatePath);
        }
        return Manual.superManual(definitionReader);
    }

    private static Schematic from(
        DefinitionReader definitionReader,
        Function<TemplateForSchematicDefinition, Template> templateFunction,
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
    public Repositories repositories() {
        return Repositories.from(
                schematicDefinition.repositories()
                    .stream()
                    .map(repositoryDefinition -> new Repository(repositoryDefinition, definitionReader()))
                    .collect(new ImmutableSetCollector<>())
            )
            .override(template.repositories());
    }

    @Override
    public Properties properties(Repositories repositories) {
        return properties(schematicDefinition.properties())
            .override(template.properties(repositories));
    }

    @Override
    public Plugins plugins(Repositories repositories) {
        return plugins(schematicDefinition.plugins())
            .override(template.plugins(repositories));
    }

    @Override
    public Dependencies dependencies(Repositories repositories, SchematicProducts schematicProducts) {
        return dependencies(schematicDefinition.dependencies(), schematicProducts)
            .override(template.dependencies(repositories, schematicProducts));
    }

    @Override
    public Optional<Schematic> root() {
        return template.root().or(() -> Optional.of(this));
    }

    @Override
    public boolean inheritsFrom(Schematic schematic) {
        return template.equals(schematic) || template.inheritsFrom(schematic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var schematic = (Schematic) object;
        return Objects.equals(path, schematic.path);
    }

    String name() {
        return schematicDefinition.name();
    }

    ImmutableList<Schematic> inclusions() {
        return schematicDefinition.inclusions()
            .stream()
            .map(inclusion -> Schematic.from(definitionReader(), this, inclusion))
            .collect(new ImmutableListCollector<>());
    }

    boolean requires(Schematic schematic, Schematics schematics) {
        return template.equals(schematic) ||
               template.inheritsFrom(schematic) ||
               dependsOn(schematic, schematics);
    }

    boolean dependsOn(Schematic schematic, Schematics schematics) {
        return schematicDefinition.dependencies()
            .stream()
            .map(this::schematicDependencyDefinition)
            .flatMap(Optional::stream)
            .map(SchematicDependencyDefinition::schematic)
            .anyMatch(name ->
                name.equals(schematic.name()) ||
                schematics.findByName(name).dependsOn(schematic, schematics)
            );
    }

    SchematicProducts construct(Repositories repositories, SchematicProducts schematicProducts, Stage stage) {
        return schematicProducts.with(
            schematicDefinition.name(),
            plugins(repositories)
                .executeTasks(
                    new Products().with(path, ProductType.SCHEMATIC_DEFINITION),
                    repositories,
                    properties(repositories).withDefaults(schematicDefinition.name(), path),
                    dependencies(repositories, schematicProducts),
                    stage
                )
        );
    }

    boolean contains(Schematic schematic) {
        return equals(schematic) ||
               inclusions()
                   .stream()
                   .anyMatch(inclusion -> inclusion.contains(schematic));
    }

    private Optional<SchematicDependencyDefinition> schematicDependencyDefinition(
        DependencyDefinition dependencyDefinition
    ) {
        if (dependencyDefinition instanceof SchematicDependencyDefinition definition) {
            return Optional.of(definition);
        }
        return Optional.empty();
    }
}
