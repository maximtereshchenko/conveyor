package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class Project {

    private final Path projectDefinitionPath;
    private final Parent parent;
    private final ProjectDefinition projectDefinition;
    private final DirectoryRepository repository;
    private final Pattern interpolationPattern;

    private Project(
        Path projectDefinitionPath,
        Parent parent,
        ProjectDefinition projectDefinition,
        DirectoryRepository repository
    ) {
        this.projectDefinitionPath = projectDefinitionPath;
        this.parent = parent;
        this.projectDefinition = projectDefinition;
        this.repository = repository;
        this.interpolationPattern = Pattern.compile("\\$\\{([^}]+)}");
    }

    static Project from(ProjectDefinitionReader projectDefinitionReader, Path projectDefinitionPath) {
        var projectDefinition = projectDefinitionReader.projectDefinition(projectDefinitionPath);
        var repository = new DirectoryRepository(
            projectDefinitionPath.getParent().resolve(projectDefinition.repository()),
            projectDefinitionReader
        );
        return new Project(
            projectDefinitionPath,
            SuperParent.from(repository),
            projectDefinition,
            repository
        );
    }

    Collection<PluginDefinition> plugins() {
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

    ProjectDefinition definition() {
        return projectDefinition;
    }

    BuildFiles executeTasks(ModuleLoader moduleLoader, Stage stage) {
        return moduleLoader.conveyorPlugins(pluginsModulePath())
            .stream()
            .map(this::bindings)
            .flatMap(Collection::stream)
            .filter(binding -> binding.stage().compareTo(stage) <= 0)
            .sorted(Comparator.comparing(ConveyorTaskBinding::stage).thenComparing(ConveyorTaskBinding::step))
            .map(ConveyorTaskBinding::task)
            .reduce(
                new BuildFiles(),
                (buildFiles, task) -> task.execute(buildFiles),
                (first, second) -> first
            );
    }

    private PluginDefinition merge(PluginDefinition declared, PluginDefinition pluginDefinition) {
        var copy = new HashMap<>(declared.configuration());
        copy.putAll(pluginDefinition.configuration());
        return new PluginDefinition(declared.name(), pluginDefinition.version(), copy);
    }

    private Set<Path> pluginsModulePath() {
        return Dependencies.forPlugins(repository, this)
            .modulePath()
            .stream()
            .map(repository::artifact)
            .collect(Collectors.toSet());
    }

    private Collection<ConveyorTaskBinding> bindings(ConveyorPlugin conveyorPlugin) {
        return conveyorPlugin.bindings(
            new ProjectConveyorPluginAdapter(projectDefinitionPath, repository, this),
            pluginConfiguration(projectDefinition, conveyorPlugin.name())
        );
    }

    private Map<String, String> pluginConfiguration(ProjectDefinition projectDefinition, String name) {
        return projectDefinition.plugins()
            .stream()
            .filter(pluginDefinition -> pluginDefinition.name().equals(name))
            .map(PluginDefinition::configuration)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .map(entry -> Map.entry(entry.getKey(), interpolate(entry.getValue(), projectDefinition.properties())))
            .collect(Collectors.collectingAndThen(Collectors.toMap(Entry::getKey, Entry::getValue), Map::copyOf));
    }

    private String interpolate(String value, Map<String, String> properties) {
        return interpolationPattern.matcher(value)
            .results()
            .reduce(
                value,
                (current, matchResult) ->
                    current.replace(matchResult.group(), properties.get(matchResult.group(1))),
                (first, second) -> first
            );
    }
}
