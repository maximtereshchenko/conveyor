package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.exception.CouldNotFindProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConveyorFacade implements ConveyorModule {

    private final ProjectDefinitionReader projectDefinitionReader;
    private final ModuleLoader moduleLoader = new ModuleLoader();

    public ConveyorFacade(ProjectDefinitionReader projectDefinitionReader) {
        this.projectDefinitionReader = projectDefinitionReader;
    }

    @Override
    public BuildFiles build(Path projectDefinitionPath, Stage stage) {
        if (!Files.exists(projectDefinitionPath)) {
            throw new CouldNotFindProjectDefinition(projectDefinitionPath);
        }
        return Project.from(projectDefinitionReader, projectDefinitionPath).executeTasks(moduleLoader, stage);
    }
}
