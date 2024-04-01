package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SchematicBuilder {

    private final GsonAdapter gsonAdapter;
    private final SchematicDefinition schematicDefinition;

    private SchematicBuilder(GsonAdapter gsonAdapter, SchematicDefinition schematicDefinition) {
        this.gsonAdapter = gsonAdapter;
        this.schematicDefinition = schematicDefinition;
    }

    SchematicBuilder(GsonAdapter gsonAdapter) {
        this(
            gsonAdapter,
            new SchematicDefinition(
                "",
                1,
                new NoExplicitTemplate(),
                List.of(),
                Paths.get(""),
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

    SchematicBuilder template(String name, int version) {
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                new ManualTemplateDefinition(name, version),
                schematicDefinition.inclusions(),
                schematicDefinition.repository(),
                schematicDefinition.properties(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder repository(Path path) {
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                path,
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

    SchematicBuilder plugin(String name, int version, Map<String, String> configuration) {
        var copy = new ArrayList<>(schematicDefinition.plugins());
        copy.add(new PluginDefinition(name, version, configuration));
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
