package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class ChildProject implements Project {

    private final Project parent;
    private final ProjectDefinition projectDefinition;

    ChildProject(Project parent, ProjectDefinition projectDefinition) {
        this.parent = parent;
        this.projectDefinition = projectDefinition;
    }

    @Override
    public String name() {
        return projectDefinition.name();
    }

    @Override
    public int version() {
        return projectDefinition.version();
    }

    @Override
    public Map<String, String> properties() {
        var copy = new HashMap<>(parent.properties());
        copy.putAll(projectDefinition.properties());
        return Map.copyOf(copy);
    }

    @Override
    public Collection<PluginDefinition> plugins() {
        var indexed = parent.plugins()
            .stream()
            .collect(Collectors.toMap(PluginDefinition::name, Function.identity()));
        for (var pluginDefinition : projectDefinition.plugins()) {
            var declared = indexed.get(pluginDefinition.name());
            if (declared == null) {
                indexed.put(pluginDefinition.name(), pluginDefinition);
            } else {
                indexed.put(pluginDefinition.name(), merge(declared, pluginDefinition));
            }
        }
        return List.copyOf(indexed.values());
    }

    @Override
    public Collection<DependencyDefinition> dependencies() {
        var indexed = parent.dependencies()
            .stream()
            .collect(Collectors.toMap(DependencyDefinition::name, Function.identity()));
        for (var dependencyDefinition : projectDefinition.dependencies()) {
            indexed.put(dependencyDefinition.name(), dependencyDefinition);
        }
        return List.copyOf(indexed.values());
    }

    private PluginDefinition merge(PluginDefinition declared, PluginDefinition pluginDefinition) {
        var copy = new HashMap<>(declared.configuration());
        copy.putAll(pluginDefinition.configuration());
        return new PluginDefinition(declared.name(), pluginDefinition.version(), copy);
    }
}
