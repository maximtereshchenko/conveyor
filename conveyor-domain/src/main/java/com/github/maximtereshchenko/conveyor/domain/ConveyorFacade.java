package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.BuildResult;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.CouldNotFindProjectDefinition;
import java.nio.file.Path;

public final class ConveyorFacade implements ConveyorModule {

    @Override
    public BuildResult build(Path projectDefinition) {
        return new CouldNotFindProjectDefinition(projectDefinition);
    }
}
