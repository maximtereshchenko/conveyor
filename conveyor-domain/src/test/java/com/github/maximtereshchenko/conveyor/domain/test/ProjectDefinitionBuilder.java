package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

final class ProjectDefinitionBuilder implements ArtifactBuilder {

    private final GsonAdapter gsonAdapter;
    private final String name;
    private final int version;
    private final Function<Path, ParentDefinition> parentFunction;
    private final Collection<ProjectDefinitionBuilder> subprojects;
    private final Map<String, String> properties;
    private final Collection<Function<Path, PluginDefinition>> plugins;
    private final Collection<Function<Path, DependencyDefinition>> dependencies;
    private final BiFunction<String, Integer, String> fileNameFunction;

    private ProjectDefinitionBuilder(
        GsonAdapter gsonAdapter,
        String name,
        int version,
        Function<Path, ParentDefinition> parentFunction,
        Collection<ProjectDefinitionBuilder> subprojects,
        Map<String, String> properties,
        Collection<Function<Path, PluginDefinition>> plugins,
        Collection<Function<Path, DependencyDefinition>> dependencies,
        BiFunction<String, Integer, String> fileNameFunction
    ) {
        this.gsonAdapter = gsonAdapter;
        this.name = name;
        this.version = version;
        this.parentFunction = parentFunction;
        this.subprojects = List.copyOf(subprojects);
        this.properties = Map.copyOf(properties);
        this.plugins = List.copyOf(plugins);
        this.dependencies = List.copyOf(dependencies);
        this.fileNameFunction = fileNameFunction;
    }

    static ProjectDefinitionBuilder empty(GsonAdapter gsonAdapter, String name) {
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            1,
            path -> new NoExplicitParent(),
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
            "%s-%d.json"::formatted
        );
    }

    static ProjectDefinitionBuilder conveyorJson(GsonAdapter gsonAdapter) {
        return empty(gsonAdapter, "project")
            .fileName("conveyor.json");
    }

    static ProjectDefinitionBuilder superParent(GsonAdapter gsonAdapter) {
        return empty(gsonAdapter, "super-parent");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public Path install(Path path) {
        var json = path.resolve(fileNameFunction.apply(name, version));
        gsonAdapter.write(
            json,
            new ProjectDefinition(
                name,
                version,
                parentFunction.apply(path),
                installedSubproject(path),
                path,
                properties,
                plugins.stream()
                    .map(function -> function.apply(path))
                    .toList(),
                dependencies.stream()
                    .map(function -> function.apply(path))
                    .toList()
            )
        );
        return json;
    }

    ProjectDefinitionBuilder name(String name) {
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            version,
            parentFunction,
            subprojects,
            properties,
            plugins,
            dependencies,
            fileNameFunction
        );
    }

    ProjectDefinitionBuilder version(int version) {
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            version,
            parentFunction,
            subprojects,
            properties,
            plugins,
            dependencies,
            fileNameFunction
        );
    }

    ProjectDefinitionBuilder parent(ProjectDefinitionBuilder builder) {
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            version,
            (path) -> parent(builder, path),
            subprojects,
            properties,
            plugins,
            dependencies,
            fileNameFunction
        );
    }

    ProjectDefinitionBuilder subproject(ProjectDefinitionBuilder builder) {
        var copy = new ArrayList<>(subprojects);
        copy.add(builder);
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            version,
            parentFunction,
            copy,
            properties,
            plugins,
            dependencies,
            fileNameFunction
        );
    }

    ProjectDefinitionBuilder property(String key, String value) {
        var copy = new HashMap<>(properties);
        copy.put(key, value);
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            version,
            parentFunction,
            subprojects,
            copy,
            plugins,
            dependencies,
            fileNameFunction
        );
    }

    ProjectDefinitionBuilder fileName(String fileName) {
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            version,
            parentFunction,
            subprojects,
            properties,
            plugins,
            dependencies,
            (projectName, version) -> fileName
        );
    }

    ProjectDefinitionBuilder plugin(ConveyorPluginBuilder builder) {
        return plugin(builder, Map.of());
    }

    ProjectDefinitionBuilder plugin(ConveyorPluginBuilder builder, Map<String, String> configuration) {
        var copy = new ArrayList<>(plugins);
        copy.add((path) -> pluginDefinition(path, builder, configuration));
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            version,
            parentFunction,
            subprojects,
            properties,
            copy,
            dependencies,
            fileNameFunction
        );
    }

    ProjectDefinitionBuilder dependency(ArtifactBuilder builder) {
        return dependency(builder, DependencyScope.IMPLEMENTATION);
    }

    ProjectDefinitionBuilder dependency(ArtifactBuilder builder, DependencyScope scope) {
        var copy = new ArrayList<>(dependencies);
        copy.add((path) -> dependencyDefinition(path, builder, scope));
        return new ProjectDefinitionBuilder(
            gsonAdapter,
            name,
            version,
            parentFunction,
            subprojects,
            properties,
            plugins,
            copy,
            fileNameFunction
        );
    }

    private Collection<Path> installedSubproject(Path path) {
        return subprojects.stream()
            .map(subproject -> subproject.install(directory(path.resolve(subproject.name()))))
            .map(Path::getParent)
            .toList();
    }

    private Path directory(Path path) {
        try {
            return Files.createDirectory(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private PluginDefinition pluginDefinition(
        Path path,
        ConveyorPluginBuilder builder,
        Map<String, String> configuration
    ) {
        builder.install(path);
        return new PluginDefinition(builder.name(), builder.version(), configuration);
    }

    private DependencyDefinition dependencyDefinition(Path path, ArtifactBuilder builder, DependencyScope scope) {
        builder.install(path);
        return new DependencyDefinition(builder.name(), builder.version(), scope);
    }

    private ParentDefinition parent(ProjectDefinitionBuilder builder, Path path) {
        builder.install(path);
        return new ParentProjectDefinition(builder.name, builder.version);
    }
}
