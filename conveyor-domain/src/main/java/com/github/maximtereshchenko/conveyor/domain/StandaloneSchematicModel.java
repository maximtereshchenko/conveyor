package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

final class StandaloneSchematicModel
    extends BaseModel<SchematicTemplateModel, DependencyModel, SchematicDependencyDefinition>
    implements SchematicModel<SchematicTemplateModel> {

    private final SchematicDefinition schematicDefinition;
    private final Path path;

    StandaloneSchematicModel(SchematicDefinition schematicDefinition, Path path) {
        this.schematicDefinition = schematicDefinition;
        this.path = path;
    }

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
    public SchematicTemplateModel template() {
        return switch (schematicDefinition.template()) {
            case ManualTemplateDefinition definition -> new OtherManualTemplateModel(
                definition.group(),
                definition.name(),
                new SemanticVersion(definition.version())
            );
            case NoTemplate ignored -> defaultTemplate();
            case SchematicPathTemplateDefinition definition ->
                new OtherSchematicTemplateModel(definition.path());
        };
    }

    @Override
    public Map<String, String> properties() {
        return schematicDefinition.properties();
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public Set<RepositoryModel> repositories() {
        return schematicDefinition.repositories()
            .stream()
            .map(repositoryDefinition ->
                switch (repositoryDefinition) {
                    case LocalDirectoryRepositoryDefinition definition ->
                        new LocalDirectoryRepositoryModel(
                            definition.name(),
                            definition.path(),
                            definition.enabled()
                        );
                    case RemoteRepositoryDefinition definition -> new RemoteRepositoryModel(
                        definition.name(),
                        definition.url(),
                        definition.enabled()
                    );
                }
            )
            .collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(schematicDefinition, path);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (StandaloneSchematicModel) object;
        return Objects.equals(schematicDefinition, that.schematicDefinition) &&
               Objects.equals(path, that.path);
    }

    LinkedHashSet<Path> inclusions() {
        return new LinkedHashSet<>(schematicDefinition.inclusions());
    }

    @Override
    PreferencesDefinition preferencesDefinition() {
        return schematicDefinition.preferences();
    }

    @Override
    Collection<PluginDefinition> pluginDefinitions() {
        return schematicDefinition.plugins();
    }

    @Override
    Collection<SchematicDependencyDefinition> dependencyDefinitions() {
        return schematicDefinition.dependencies();
    }

    @Override
    DependencyModel dependencyModel(SchematicDependencyDefinition dependencyDefinition) {
        return switch (dependencyDefinition) {
            case DependencyOnArtifactDefinition definition -> new ArtifactDependencyModel(
                definition.group(),
                definition.name(),
                definition.version(),
                definition.scope()
            );
            case DependencyOnSchematicDefinition definition ->
                new SchematicDependencyModel(group(), definition.schematic(), definition.scope());
        };
    }

    private SchematicTemplateModel defaultTemplate() {
        var defaultSchematicTemplate = path.getParent().getParent().resolve("conveyor.json");
        if (Files.exists(defaultSchematicTemplate)) {
            return new OtherSchematicTemplateModel(defaultSchematicTemplate);
        }
        return new OtherManualTemplateModel(
            "com.github.maximtereshchenko.conveyor",
            "super-manual",
            new SemanticVersion("1.0.0")
        );
    }
}
