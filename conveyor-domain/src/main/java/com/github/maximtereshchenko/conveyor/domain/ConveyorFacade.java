package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.BuildFailedWithException;
import com.github.maximtereshchenko.conveyor.api.BuildResult;
import com.github.maximtereshchenko.conveyor.api.BuildSucceeded;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConveyorFacade implements ConveyorModule {

    private final Gson gson = new Gson();

    @Override
    public BuildResult build(Path projectDefinitionPath) {
        if (!Files.exists(projectDefinitionPath)) {
            return new CouldNotFindProjectDefinition(projectDefinitionPath);
        }
        try (var reader = Files.newBufferedReader(projectDefinitionPath)) {
            var projectDefinition = gson.fromJson(reader, ProjectDefinition.class);
            return new BuildSucceeded(projectDefinitionPath, projectDefinition.name(), projectDefinition.version());
        } catch (IOException e) {
            return new BuildFailedWithException(e);
        }
    }
}
