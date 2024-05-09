package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.api.schematic.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

final class SchematicDefinitionBuilder {

    private final List<ArtifactPreferenceDefinition> artifactPreferences = new ArrayList<>();
    private final List<DependencyDefinition> dependencies = new ArrayList<>();
    private final List<Path> inclusions = new ArrayList<>();
    private final JacksonAdapter jacksonAdapter;
    private final List<PluginDefinition> plugins = new ArrayList<>();
    private final List<PreferencesInclusionDefinition> preferenceInclusions =
        new ArrayList<>();
    private final Map<String, String> properties = new HashMap<>();
    private final List<RepositoryDefinition> repositories = new ArrayList<>();
    private String group = "group";
    private String name = "project";
    private String version = "1.0.0";
    private TemplateDefinition template = new NoTemplateDefinition();

    SchematicDefinitionBuilder(JacksonAdapter jacksonAdapter) {
        this.jacksonAdapter = jacksonAdapter;
    }

    void write(Path path) throws IOException {
        try (var outputStream = Files.newOutputStream(path)) {
            outputStream.write(
                jacksonAdapter.bytes(
                    new SchematicDefinition(
                        Optional.ofNullable(group),
                        name,
                        Optional.ofNullable(version),
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
                    )
                )
            );
        }
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
        return repository(path.toString(), path);
    }

    SchematicDefinitionBuilder repository(String name, Path path) {
        repositories.add(
            new LocalDirectoryRepositoryDefinition(name, path)
        );
        return this;
    }

    SchematicDefinitionBuilder repository(String uri) {
        return repository(uri, uri);
    }

    SchematicDefinitionBuilder repository(String name, String uri) {
        repositories.add(
            new RemoteRepositoryDefinition(name, URI.create(uri))
        );
        return this;
    }

    Path conveyorJson(Path directory) throws IOException {
        var path = Directories.createDirectoriesForFile(directory.resolve("conveyor.json"));
        write(path);
        return path;
    }

    SchematicDefinitionBuilder template(String name) {
        return template(group, name, version);
    }

    SchematicDefinitionBuilder template(String group, String name, String version) {
        template = new SchematicTemplateDefinition(group, name, version);
        return this;
    }

    SchematicDefinitionBuilder plugin(String name) {
        return plugin(group, name, version, Map.of());
    }

    SchematicDefinitionBuilder plugin(
        String group,
        String name,
        String version,
        Map<String, String> configuration
    ) {
        plugins.add(new PluginDefinition(group, name, Optional.ofNullable(version), configuration));
        return this;
    }

    SchematicDefinitionBuilder group(String group) {
        this.group = group;
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
        return dependency(group, name, version, DependencyScope.IMPLEMENTATION);
    }

    SchematicDefinitionBuilder dependency(
        String group,
        String name,
        String version,
        DependencyScope scope,
        ExclusionDefinition... exclusionDefinitions
    ) {
        dependencies.add(
            new DependencyDefinition(
                group,
                name,
                Optional.ofNullable(version),
                Optional.of(scope),
                List.of(exclusionDefinitions)
            )
        );
        return this;
    }

    SchematicDefinitionBuilder inclusion(Path path) {
        inclusions.add(path);
        return this;
    }

    SchematicDefinitionBuilder preference(String name, String version) {
        return preference(group, name, version);
    }

    SchematicDefinitionBuilder preference(String group, String name, String version) {
        artifactPreferences.add(new ArtifactPreferenceDefinition(group, name, version));
        return this;
    }

    SchematicDefinitionBuilder preferenceInclusion(String group, String name, String version) {
        preferenceInclusions.add(new PreferencesInclusionDefinition(group, name, version));
        return this;
    }

    SchematicDefinitionBuilder preferenceInclusion(String name) {
        return preferenceInclusion(group, name, version);
    }

    SchematicDefinitionBuilder property(String key, String value) {
        properties.put(key, value);
        return this;
    }
}
