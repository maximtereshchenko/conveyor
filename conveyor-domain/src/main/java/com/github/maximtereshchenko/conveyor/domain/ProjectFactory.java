package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.*;

import java.nio.file.Path;
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
        )
            .sorted(this::comparedByMutualDependency)
            .toList();
    }

    private int comparedByMutualDependency(LocalProject first, LocalProject second) {
        if (first.dependsOn(second)) {
            return 1;
        }
        if (second.dependsOn(first)) {
            return -1;
        }
        return 0;
    }

    private Stream<LocalProject> localProjects(
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
                .flatMap(conveyorJson ->
                    localProjects(
                        conveyorJson,
                        repository,
                        reader.projectDefinition(conveyorJson),
                        current
                    )
                )
        );
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
