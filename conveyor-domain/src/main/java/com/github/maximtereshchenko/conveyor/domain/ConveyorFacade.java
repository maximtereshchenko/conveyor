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
import java.util.Set;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final JsonReader jsonReader;
    private final ModuleLoader moduleLoader = new ModuleLoader();

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
                projectDefinition.pluginConfiguration(conveyorPlugin.name())
            )
        );
    }
}
