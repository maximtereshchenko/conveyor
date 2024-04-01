package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.BuildResult;
import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPluginConfiguration;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;

public final class ConveyorFacade implements ConveyorModule {

    private final ProjectDefinitionReader projectDefinitionReader;

    public ConveyorFacade(ProjectDefinitionReader projectDefinitionReader) {
        this.projectDefinitionReader = projectDefinitionReader;
    }

    @Override
    public BuildResult build(Path projectDefinitionPath) {
        if (!Files.exists(projectDefinitionPath)) {
            return new CouldNotFindProjectDefinition(projectDefinitionPath);
        }
        var projectDefinition = projectDefinitionReader.projectDefinition(projectDefinitionPath);
        var repository = new DirectoryRepository(projectDefinition.repository());
        projectDefinition.plugins()
            .stream()
            .map(pluginDefinition -> tasks(projectDefinitionPath, pluginDefinition, repository))
            .flatMap(Collection::stream)
            .forEach(ConveyorTask::execute);
        return new BuildSucceeded(projectDefinitionPath, projectDefinition.name(), projectDefinition.version());
    }

    private Collection<ConveyorTask> tasks(
        Path projectDefinitionPath,
        PluginDefinition pluginDefinition,
        DirectoryRepository repository
    ) {
        return plugin(repository, pluginDefinition)
            .tasks(
                new ConveyorPluginConfiguration(
                    projectDefinitionPath.getParent(),
                    pluginDefinition.configuration()
                )
            );
    }

    private ConveyorPlugin plugin(DirectoryRepository repository, PluginDefinition pluginDefinition) {
        return ServiceLoader.load(moduleLayer(repository, pluginDefinition), ConveyorPlugin.class)
            .findFirst()
            .orElseThrow();
    }

    private ModuleLayer moduleLayer(DirectoryRepository repository, PluginDefinition pluginDefinition) {
        var parent = getClass().getModule().getLayer();
        var configuration = parent.configuration()
            .resolveAndBind(
                ModuleFinder.of(repository.artifact(pluginDefinition.name(), pluginDefinition.version())),
                ModuleFinder.of(),
                Set.of()
            );
        return parent.defineModulesWithOneLoader(configuration, Thread.currentThread().getContextClassLoader());
    }
}
