package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

final class ProjectFactory {

    private final ProjectDefinitionReader reader;

    ProjectFactory(ProjectDefinitionReader reader) {
        this.reader = reader;
    }

    List<LocalProject> localProjects(Path projectDefinitionPath) {
        var projectDefinition = reader.projectDefinition(projectDefinitionPath);
        var repository = new DirectoryRepository(projectDefinition.repository(), reader);
        return localProjects(
            projectDefinitionPath,
            repository,
            projectDefinition,
            project(repository, projectDefinition.parent())
        );
    }

    private List<LocalProject> localProjects(
        Path projectDefinitionPath,
        DirectoryRepository repository,
        ProjectDefinition projectDefinition,
        Project parent
    ) {
        var current = new Subproject(parent, projectDefinition);
        return Stream.concat(
                Stream.of(new LocalProject(current, repository, projectDefinitionPath)),
                projectDefinition.subprojects()
                    .stream()
                    .map(path -> projectDefinitionPath.getParent().resolve(path).resolve("conveyor.json"))
                    .map(conveyorJson ->
                        localProjects(
                            conveyorJson,
                            repository,
                            reader.projectDefinition(conveyorJson),
                            current
                        )
                    )
                    .flatMap(Collection::stream)
            )
            .toList();
    }

    private Project project(DirectoryRepository repository, ParentDefinition parentDefinition) {
        return switch (parentDefinition) {
            case NoExplicitParent ignored -> SuperParent.from(repository);
            case ParentProjectDefinition parentProjectDefinition ->
                project(repository, repository.projectDefinition(parentProjectDefinition));
        };
    }

    private Project project(DirectoryRepository repository, ProjectDefinition projectDefinition) {
        return new Subproject(
            project(repository, projectDefinition.parent()),
            projectDefinition
        );
    }
}
