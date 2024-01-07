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
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.api.Stage;
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
    public BuildResult build(Path projectDefinitionPath, Stage stage) {
        if (!Files.exists(projectDefinitionPath)) {
            return new CouldNotFindProjectDefinition(projectDefinitionPath);
        }
        var projectDefinition = projectDefinitionReader.projectDefinition(projectDefinitionPath);
        var repository = new DirectoryRepository(
            projectDefinitionPath.getParent().resolve(projectDefinition.repository()));
        projectDefinition.plugins()
            .stream()
            .map(pluginDefinition -> bindings(projectDefinitionPath, pluginDefinition, repository))
            .flatMap(Collection::stream)
            .filter(binding -> binding.stage().compareTo(stage) <= 0)
            .map(ConveyorTaskBinding::task)
            .forEach(ConveyorTask::execute);
        return new BuildSucceeded(projectDefinitionPath, projectDefinition.name(), projectDefinition.version());
    }

    private Collection<ConveyorTaskBinding> bindings(
        Path projectDefinitionPath,
        PluginDefinition pluginDefinition,
        DirectoryRepository repository
    ) {
        return plugin(repository, pluginDefinition)
            .bindings(
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
