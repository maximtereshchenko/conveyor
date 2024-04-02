package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.schematic.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

final class SchematicDefinitionBuilder {

    private final List<ArtifactPreferenceDefinition> artifactPreferences = new ArrayList<>();
    private final List<DependencyDefinition> dependencies = new ArrayList<>();
    private final String group = "com.github.maximtereshchenko.conveyor";
    private final List<Path> inclusions = new ArrayList<>();
    private final JacksonAdapter jacksonAdapter;
    private final List<PluginDefinition> plugins = new ArrayList<>();
    private final List<PreferencesInclusionDefinition> preferenceInclusions =
        new ArrayList<>();
    private final Map<String, String> properties = new HashMap<>();
    private final List<RepositoryDefinition> repositories = new ArrayList<>();
    private String name = "project";
    private String version = "1.0.0";
    private TemplateDefinition template = new NoTemplateDefinition();

    SchematicDefinitionBuilder(JacksonAdapter jacksonAdapter) {
        this.jacksonAdapter = jacksonAdapter;
    }

    void install(OutputStream outputStream) {
        jacksonAdapter.write(
            new SchematicDefinition(
                group,
                name,
                version,
                template,
                inclusions,
                repositories,
                properties,
                new PreferencesDefinition(
                    preferenceInclusions,
                    artifactPreferences
                ),
                plugins,
                dependencies
            ),
            outputStream
        );
    }

    String group() {
        return group;
    }

    String name() {
        return name;
    }

    String version() {
        return version;
    }

    SchematicDefinitionBuilder repository(Path path) {
        return repository(path.toString(), path, true);
    }

    SchematicDefinitionBuilder repository(String name, Path path, boolean enabled) {
        repositories.add(
            new LocalDirectoryRepositoryDefinition(name, path, Optional.of(enabled))
        );
        return this;
    }

    SchematicDefinitionBuilder repository(String name, String url, boolean enabled) {
        try {
            repositories.add(
                new RemoteRepositoryDefinition(name, URI.create(url).toURL(), Optional.of(enabled))
            );
            return this;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    Path install(Path directory) {
        var path = directory.resolve("conveyor.json");
        try (var outputStream = outputStream(path)) {
            install(outputStream);
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    SchematicDefinitionBuilder template(String name) {
        template = new SchematicTemplateDefinition(group, name, version);
        return this;
    }

    SchematicDefinitionBuilder plugin(String name) {
        return plugin(name, version, Map.of());
    }

    SchematicDefinitionBuilder plugin(String name, Map<String, String> configuration) {
        return plugin(name, null, configuration);
    }

    SchematicDefinitionBuilder plugin(
        String name,
        String version,
        Map<String, String> configuration
    ) {
        plugins.add(new PluginDefinition(group, name, Optional.ofNullable(version), configuration));
        return this;
    }

    SchematicDefinitionBuilder name(String name) {
        this.name = name;
        return this;
    }

    SchematicDefinitionBuilder version(String version) {
        this.version = version;
        return this;
    }

    SchematicDefinitionBuilder dependency(String name) {
        return dependency(name, version, DependencyScope.IMPLEMENTATION);
    }

    SchematicDefinitionBuilder dependency(String name, DependencyScope scope) {
        return dependency(name, null, scope);
    }

    SchematicDefinitionBuilder dependency(String name, String version, DependencyScope scope) {
        dependencies.add(
            new DependencyDefinition(
                group,
                name,
                Optional.ofNullable(version),
                Optional.of(scope)
            )
        );
        return this;
    }

    SchematicDefinitionBuilder inclusion(Path path) {
        inclusions.add(path);
        return this;
    }

    SchematicDefinitionBuilder preference(String name, String version) {
        artifactPreferences.add(new ArtifactPreferenceDefinition(group, name, version));
        return this;
    }

    SchematicDefinitionBuilder preferenceInclusion(String name, String version) {
        preferenceInclusions.add(new PreferencesInclusionDefinition(group, name, version));
        return this;
    }

    SchematicDefinitionBuilder preferenceInclusion(String name) {
        return preferenceInclusion(name, version);
    }

    SchematicDefinitionBuilder property(String key, String value) {
        properties.put(key, value);
        return this;
    }

    private OutputStream outputStream(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.newOutputStream(path);
    }
}
