package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.Project;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

final class ProjectConveyorPluginAdapter implements Project {

    private final Path projectDefinitionPath;
    private final DirectoryRepository repository;
    private final ProjectDefinition projectDefinition;

    ProjectConveyorPluginAdapter(
        Path projectDefinitionPath,
        DirectoryRepository repository,
        ProjectDefinition projectDefinition
    ) {
        this.projectDefinitionPath = projectDefinitionPath;
        this.repository = repository;
        this.projectDefinition = projectDefinition;
    }

    @Override
    public Path projectDirectory() {
        var projectDirectory = projectDefinition.properties().get("conveyor.project.directory");
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
                        projectDefinition.properties()
                            .getOrDefault("conveyor.project.build.directory", ".conveyor")
                    )
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Set<Path> modulePath(DependencyScope... scopes) {
        return Dependencies.forDependencies(repository, projectDefinition, scopes)
            .modulePath()
            .stream()
            .map(repository::artifact)
            .collect(Collectors.toSet());
    }
}
