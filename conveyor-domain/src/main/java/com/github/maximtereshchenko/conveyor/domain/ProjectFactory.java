package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.NoExplicitParent;
import com.github.maximtereshchenko.conveyor.api.port.ParentProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import java.nio.file.Path;

final class ProjectFactory {

    private final ProjectDefinitionReader reader;

    ProjectFactory(ProjectDefinitionReader reader) {
        this.reader = reader;
    }

    ProjectToBuild projectToBuild(Path projectDefinitionPath) {
        var projectDefinition = reader.projectDefinition(projectDefinitionPath);
        var repository = new DirectoryRepository(
            projectDefinitionPath.getParent().resolve(projectDefinition.repository()),
            reader
        );
        return new ProjectToBuild(
            projectDefinitionPath,
            project(
                repository,
                projectDefinition,
                projectDefinitionPath.getParent()
            ),
            repository
        );
    }

    private Project project(
        DirectoryRepository repository,
        ProjectDefinition projectDefinition,
        Path directory
    ) {
        return new Subproject(
            switch (projectDefinition.parent()) {
                case NoExplicitParent ignored -> SuperParent.from(repository);
                case ParentProjectDefinition parentProjectDefinition ->
                    project(repository, repository.projectDefinition(parentProjectDefinition), directory);
            },
            projectDefinition.subprojects()
                .stream()
                .map(directory::resolve)
                .map(path -> path.resolve("conveyor.json"))
                .map(conveyorJson -> project(repository, reader.projectDefinition(conveyorJson),
                    conveyorJson.getParent()
                ))
                .toList(),
            projectDefinition
        );
    }
}
