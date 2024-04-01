package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.gson.JacksonAdapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

final class SchematicBuilder {

    private final JacksonAdapter gsonAdapter;
    private final SchematicDefinition schematicDefinition;

    private SchematicBuilder(JacksonAdapter gsonAdapter, SchematicDefinition schematicDefinition) {
        this.gsonAdapter = gsonAdapter;
        this.schematicDefinition = schematicDefinition;
    }

    SchematicBuilder(JacksonAdapter gsonAdapter) {
        this(
            gsonAdapter,
            new SchematicDefinition(
                "",
                0,
                new NoExplicitlyDefinedTemplate(),
                List.of(),
                Optional.empty(),
                Map.of(),
                List.of(),
                List.of()
            )
        );
    }

    SchematicBuilder name(String name) {
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                name,
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repository(),
                schematicDefinition.properties(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder version(int version) {
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                version,
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repository(),
                schematicDefinition.properties(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder template(Path path) {
        return template(new SchematicPathTemplateDefinition(path));
    }

    SchematicBuilder template(String name, int version) {
        return template(new ManualTemplateDefinition(name, version));
    }

    SchematicBuilder repository(Path path) {
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                Optional.of(path),
                schematicDefinition.properties(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder inclusion(Path path) {
        var copy = new ArrayList<>(schematicDefinition.inclusions());
        copy.add(path);
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                copy,
                schematicDefinition.repository(),
                schematicDefinition.properties(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder plugin(String name, Map<String, String> configuration) {
        return plugin(new PluginDefinition(name, OptionalInt.empty(), configuration));
    }

    SchematicBuilder plugin(String name, int version, Map<String, String> configuration) {
        return plugin(new PluginDefinition(name, OptionalInt.of(version), configuration));
    }

    SchematicBuilder plugin(PluginDefinition pluginDefinition) {
        var copy = new ArrayList<>(schematicDefinition.plugins());
        copy.add(pluginDefinition);
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repository(),
                schematicDefinition.properties(),
                copy,
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder schematicDependency(String name, DependencyScope scope) {
        return dependency(new SchematicDependencyDefinition(name, scope));
    }

    SchematicBuilder dependency(String name, int version, DependencyScope scope) {
        return dependency(new ArtifactDependencyDefinition(name, version, scope));
    }

    SchematicBuilder property(String key, String value) {
        var copy = new HashMap<>(schematicDefinition.properties());
        copy.put(key, value);
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repository(),
                copy,
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    Path install(Path directory) {
        try {
            Files.createDirectories(directory);
            var path = directory.resolve("conveyor.json");
            gsonAdapter.write(path, schematicDefinition);
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private SchematicBuilder template(TemplateForSchematicDefinition templateDefinition) {
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                templateDefinition,
                schematicDefinition.inclusions(),
                schematicDefinition.repository(),
                schematicDefinition.properties(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    private SchematicBuilder dependency(DependencyDefinition dependencyDefinition) {
        var copy = new ArrayList<>(schematicDefinition.dependencies());
        copy.add(dependencyDefinition);
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repository(),
                schematicDefinition.properties(),
                schematicDefinition.plugins(),
                copy
            )
        );
    }
}
