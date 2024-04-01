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
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final JsonReader jsonReader;

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
        var artifacts = pluginsWithDependencies(repository, projectDefinition.plugins())
            .stream()
            .map(artifactDefinition -> repository.artifact(artifactDefinition.name(), artifactDefinition.version()))
            .toList();
        ServiceLoader.load(moduleLayer(artifacts), ConveyorPlugin.class)
            .stream()
            .map(Provider::get)
            .map(conveyorPlugin -> bindings(conveyorPlugin, projectDefinitionPath, projectDefinition))
            .flatMap(Collection::stream)
            .filter(binding -> binding.stage().compareTo(stage) <= 0)
            .map(ConveyorTaskBinding::task)
            .forEach(ConveyorTask::execute);
        return new BuildSucceeded(projectDefinitionPath, projectDefinition.name(), projectDefinition.version());
    }

    private Collection<ArtifactDefinition> pluginsWithDependencies(
        DirectoryRepository repository,
        Collection<PluginDefinition> plugins
    ) {
        return dependencies(
            plugins.stream()
                .map(pluginDefinition ->
                    repository.artifactDefinition(pluginDefinition.name(), pluginDefinition.version())
                )
                .collect(Collectors.toMap(ArtifactDefinition::name, Function.identity())),
            repository
        )
            .values();
    }

    private Map<String, ArtifactDefinition> dependencies(
        Map<String, ArtifactDefinition> collected,
        DirectoryRepository repository
    ) {
        for (var artifactDefinition : collected.values()) {
            for (var dependencyDefinition : artifactDefinition.dependencies()) {
                if (!collected.containsKey(dependencyDefinition.name()) ||
                    collected.get(dependencyDefinition.name()).version() < dependencyDefinition.version()) {
                    var copy = new HashMap<>(collected);
                    copy.put(
                        dependencyDefinition.name(),
                        repository.artifactDefinition(dependencyDefinition.name(), dependencyDefinition.version())
                    );
                    return dependencies(copy, repository);
                }
            }
        }
        return collected;
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

    private ModuleLayer moduleLayer(Collection<Path> artifacts) {
        var parent = getClass().getModule().getLayer();
        var configuration = parent.configuration()
            .resolveAndBind(
                ModuleFinder.of(artifacts.toArray(Path[]::new)),
                ModuleFinder.of(),
                Set.of()
            );
        return parent.defineModulesWithOneLoader(configuration, Thread.currentThread().getContextClassLoader());
    }
}
