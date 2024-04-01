package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.ProjectBuildFiles;
import com.github.maximtereshchenko.conveyor.api.exception.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConveyorFacade implements ConveyorModule {

    private final ProjectFactory projectFactory;
    private final ModuleLoader moduleLoader;
    private final InterpolationService interpolationService;

    public ConveyorFacade(ProjectDefinitionReader projectDefinitionReader) {
        this.projectFactory = new ProjectFactory(projectDefinitionReader);
        this.moduleLoader = new ModuleLoader();
        this.interpolationService = new InterpolationService();
    }

    @Override
    public ProjectBuildFiles build(Path projectDefinitionPath, Stage stage) {
        if (!Files.exists(projectDefinitionPath)) {
            throw new CouldNotFindProjectDefinition(projectDefinitionPath);
        }
        return projectFactory.localProjects(projectDefinitionPath)
            .stream()
            .reduce(
                new ProjectBuildFiles(),
                (buildFiles, project) ->
                    buildFiles.with(project.name(), project.build(moduleLoader, interpolationService, stage)),
                new PickSecond<>()
            );
    }
}
