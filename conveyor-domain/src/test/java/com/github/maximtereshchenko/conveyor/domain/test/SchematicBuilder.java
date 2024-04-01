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
                "",
                new NoExplicitlyDefinedTemplate(),
                List.of(),
                List.of(),
                Map.of(),
                new PreferencesDefinition(),
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
                schematicDefinition.repositories(),
                schematicDefinition.properties(),
                schematicDefinition.preferences(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder version(String version) {
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                version,
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repositories(),
                schematicDefinition.properties(),
                schematicDefinition.preferences(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder template(Path path) {
        return template(new SchematicPathTemplateDefinition(path));
    }

    SchematicBuilder template(String name, String version) {
        return template(new ManualTemplateDefinition(name, version));
    }

    SchematicBuilder repository(String name, Path path, boolean enabled) {
        var copy = new ArrayList<>(schematicDefinition.repositories());
        copy.add(new RepositoryDefinition(name, path, Optional.of(enabled)));
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                copy,
                schematicDefinition.properties(),
                schematicDefinition.preferences(),
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
                schematicDefinition.repositories(),
                schematicDefinition.properties(),
                schematicDefinition.preferences(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder plugin(String name, Map<String, String> configuration) {
        return plugin(new PluginDefinition(name, Optional.empty(), configuration));
    }

    SchematicBuilder plugin(String name, String version, Map<String, String> configuration) {
        return plugin(new PluginDefinition(name, Optional.of(version), configuration));
    }

    SchematicBuilder schematicDependency(String name, DependencyScope scope) {
        return dependency(new DependencyOnSchematicDefinition(name, Optional.of(scope)));
    }

    SchematicBuilder dependency(String name, String version, DependencyScope scope) {
        return dependency(new DependencyOnArtifactDefinition(name, Optional.of(version), Optional.of(scope)));
    }

    SchematicBuilder dependency(String name, DependencyScope scope) {
        return dependency(new DependencyOnArtifactDefinition(name, Optional.empty(), Optional.of(scope)));
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
                schematicDefinition.repositories(),
                copy,
                schematicDefinition.preferences(),
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

    SchematicBuilder preference(String name, String version) {
        var copy = new ArrayList<>(schematicDefinition.preferences().artifacts());
        copy.add(new ArtifactPreferenceDefinition(name, version));
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repositories(),
                schematicDefinition.properties(),
                new PreferencesDefinition(schematicDefinition.preferences().inclusions(), copy),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    SchematicBuilder preferenceInclusion(String name, String version) {
        var copy = new ArrayList<>(schematicDefinition.preferences().inclusions());
        copy.add(new PreferencesInclusionDefinition(name, version));
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repositories(),
                schematicDefinition.properties(),
                new PreferencesDefinition(copy, schematicDefinition.preferences().artifacts()),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    private SchematicBuilder plugin(PluginDefinition pluginDefinition) {
        var copy = new ArrayList<>(schematicDefinition.plugins());
        copy.add(pluginDefinition);
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repositories(),
                schematicDefinition.properties(),
                schematicDefinition.preferences(),
                copy,
                schematicDefinition.dependencies()
            )
        );
    }

    private SchematicBuilder template(TemplateForSchematicDefinition templateDefinition) {
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                templateDefinition,
                schematicDefinition.inclusions(),
                schematicDefinition.repositories(),
                schematicDefinition.properties(),
                schematicDefinition.preferences(),
                schematicDefinition.plugins(),
                schematicDefinition.dependencies()
            )
        );
    }

    private SchematicBuilder dependency(SchematicDependencyDefinition dependencyDefinition) {
        var copy = new ArrayList<>(schematicDefinition.dependencies());
        copy.add(dependencyDefinition);
        return new SchematicBuilder(
            gsonAdapter,
            new SchematicDefinition(
                schematicDefinition.name(),
                schematicDefinition.version(),
                schematicDefinition.template(),
                schematicDefinition.inclusions(),
                schematicDefinition.repositories(),
                schematicDefinition.properties(),
                schematicDefinition.preferences(),
                schematicDefinition.plugins(),
                copy
            )
        );
    }
}
