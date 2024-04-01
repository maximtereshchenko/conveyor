package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

final class ProjectConveyorPluginAdapter implements ConveyorProject {

    private final Path projectDefinitionPath;
    private final DirectoryRepository repository;
    private final Project project;

    ProjectConveyorPluginAdapter(
        Path projectDefinitionPath,
        DirectoryRepository repository,
        Project project
    ) {
        this.projectDefinitionPath = projectDefinitionPath;
        this.repository = repository;
        this.project = project;
    }

    @Override
    public Path projectDirectory() {
        var projectDirectory = project.definition().properties().get("conveyor.project.directory");
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
                        project.definition().properties()
                            .getOrDefault("conveyor.project.build.directory", ".conveyor")
                    )
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Set<Path> modulePath(DependencyScope... scopes) {
        return Dependencies.forDependencies(repository, project, scopes)
            .modulePath()
            .stream()
            .map(repository::artifact)
            .collect(Collectors.toSet());
    }
}
