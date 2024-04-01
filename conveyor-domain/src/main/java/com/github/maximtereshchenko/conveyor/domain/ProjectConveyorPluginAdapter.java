package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.Project;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

final class ProjectConveyorPluginAdapter implements Project {

    private final Path projectDirectory;
    private final DirectoryRepository repository;
    private final ProjectDefinition projectDefinition;

    ProjectConveyorPluginAdapter(
        Path projectDirectory,
        DirectoryRepository repository,
        ProjectDefinition projectDefinition
    ) {
        this.projectDirectory = projectDirectory;
        this.repository = repository;
        this.projectDefinition = projectDefinition;
    }

    @Override
    public Path projectDirectory() {
        return projectDirectory;
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
