package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.BuildResult;
import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.JsonReader;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPluginConfiguration;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.api.Stage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final JsonReader jsonReader;
    private final ModuleLoader moduleLoader = new ModuleLoader();
    private final Pattern interpolationPattern = Pattern.compile("\\$\\{([^}]+)}");

    public ConveyorFacade(JsonReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    @Override
    public BuildResult build(Path projectDefinitionPath, Stage stage) {
        if (!Files.exists(projectDefinitionPath)) {
            return new CouldNotFindProjectDefinition(projectDefinitionPath);
        }
        var projectDefinition = jsonReader.read(projectDefinitionPath, ProjectDefinition.class);
        var repository = new DirectoryRepository(
            projectDefinitionPath.getParent().resolve(projectDefinition.repository()),
            jsonReader
        );
        executeTasks(projectDefinitionPath, stage, repository, projectDefinition);
        return new BuildSucceeded(projectDefinitionPath, projectDefinition.name(), projectDefinition.version());
    }

    Set<Path> pluginsModulePath(DirectoryRepository repository, ProjectDefinition projectDefinition) {
        return Dependencies.forPlugins(repository, projectDefinition)
            .modulePath(projectDefinition)
            .stream()
            .map(repository::artifact)
            .collect(Collectors.toSet());
    }

    private void executeTasks(
        Path projectDefinitionPath,
        Stage stage,
        DirectoryRepository repository,
        ProjectDefinition projectDefinition
    ) {
        moduleLoader.conveyorPlugins(pluginsModulePath(repository, projectDefinition))
            .stream()
            .map(conveyorPlugin -> bindings(conveyorPlugin, projectDefinitionPath, projectDefinition))
            .flatMap(Collection::stream)
            .filter(binding -> binding.stage().compareTo(stage) <= 0)
            .map(ConveyorTaskBinding::task)
            .forEach(ConveyorTask::execute);
    }

    private Collection<ConveyorTaskBinding> bindings(
        ConveyorPlugin conveyorPlugin,
        Path projectDefinitionPath,
        ProjectDefinition projectDefinition
    ) {
        return conveyorPlugin.bindings(
            new ConveyorPluginConfiguration(
                projectDefinitionPath.getParent(),
                pluginConfiguration(projectDefinition, conveyorPlugin.name())
            )
        );
    }

    private Map<String, String> pluginConfiguration(ProjectDefinition projectDefinition, String name) {
        return projectDefinition.pluginConfiguration(name)
            .entrySet()
            .stream()
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
