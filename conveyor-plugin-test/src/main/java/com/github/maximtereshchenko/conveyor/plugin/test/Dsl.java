package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Dsl {

    private final ConveyorPlugin plugin;
    private final Path path;
    private final Map<Path, DependencyScope> dependencies = new HashMap<>();
    private final Map<String, String> configuration = new HashMap<>();

    public Dsl(ConveyorPlugin plugin, Path path) {
        this.plugin = plugin;
        this.path = path;
    }

    public Dsl givenConfiguration(String key) {
        return givenConfiguration(key, "");
    }

    public Dsl givenConfiguration(String key, Path value) {
        return givenConfiguration(key, value.toString());
    }

    public Dsl givenConfiguration(String key, String value) {
        configuration.put(key, value);
        return this;
    }

    public Dsl givenDependencies(Set<Path> paths) {
        paths.forEach(this::givenDependency);
        return this;
    }

    public Dsl givenDependency(Path path) {
        return givenDependency(path, DependencyScope.IMPLEMENTATION);
    }

    public Dsl givenDependency(Path path, DependencyScope scope) {
        dependencies.put(path, scope);
        return this;
    }

    public ConveyorTasks tasks() throws IOException {
        var conveyorJson = path.resolve("conveyor.json");
        if (!Files.exists(conveyorJson)) {
            Files.createFile(conveyorJson);
        }
        var schematic = new FakeConveyorSchematic(conveyorJson, dependencies);
        return new ConveyorTasks(plugin.tasks(schematic, configuration), schematic);
    }
}
