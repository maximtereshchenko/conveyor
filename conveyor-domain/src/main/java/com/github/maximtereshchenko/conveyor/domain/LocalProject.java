package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProject;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

final class LocalProject implements ConveyorProject {

    private final Project project;
    private final DirectoryRepository repository;
    private final Path projectDefinitionPath;

    LocalProject(Project project, DirectoryRepository repository, Path projectDefinitionPath) {
        this.project = project;
        this.repository = repository;
        this.projectDefinitionPath = projectDefinitionPath;
    }

    @Override
    public String name() {
        return project.name();
    }

    @Override
    public Path projectDirectory() {
        var projectDirectory = project.properties().get("conveyor.project.directory");
        if (projectDirectory == null) {
            return projectDefinitionPath.getParent();
        }
        return Paths.get(projectDirectory);
    }

    @Override
    public Path buildDirectory() {
        try {
            return Files.createDirectories(
                projectDirectory()
                    .resolve(
                        project.properties()
                            .getOrDefault("conveyor.project.build.directory", ".conveyor")
                    )
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Set<Path> modulePath(DependencyScope... scopes) {
        return modulePath(project.dependencies(Set.of(scopes)));
    }

    public BuildFiles build(ModuleLoader moduleLoader, InterpolationService interpolationService, Stage stage) {
        return moduleLoader.conveyorPlugins(pluginsModulePath())
            .stream()
            .map(plugin -> plugin.bindings(this, pluginConfiguration(plugin.name(), interpolationService)))
            .flatMap(Collection::stream)
            .filter(binding -> binding.stage().compareTo(stage) <= 0)
            .sorted(Comparator.comparing(ConveyorTaskBinding::stage).thenComparing(ConveyorTaskBinding::step))
            .map(ConveyorTaskBinding::task)
            .reduce(
                new BuildFiles(),
                (buildFiles, task) -> task.execute(buildFiles),
                new PickSecond<>()
            );
    }

    boolean dependsOn(LocalProject localProject) {
        return project.dependsOn(localProject.name());
    }

    private Set<Path> pluginsModulePath() {
        return modulePath(project.plugins());
    }

    private Set<Path> modulePath(Collection<? extends ArtifactDefinition> artifactDefinitions) {
        return Dependencies.from(
                artifactDefinitions.stream()
                    .map(definition -> new Artifact(definition, repository))
                    .toList()
            )
            .modulePath();
    }

    private Map<String, String> pluginConfiguration(String name, InterpolationService interpolationService) {
        return interpolationService.interpolate(
            project.properties(),
            project.plugins()
                .stream()
                .filter(pluginDefinition -> pluginDefinition.name().equals(name))
                .map(PluginDefinition::configuration)
                .findAny()
                .orElseThrow()
        );
    }
}
