package com.github.maximtereshchenko.conveyor.cli;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.core.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;

import java.nio.file.FileSystems;

final class Main {

    public static void main(String[] args) {
        var fileSystem = FileSystems.getDefault();
        new ConveyorFacade(JacksonAdapter.configured(fileSystem))
            .construct(
                fileSystem.getPath(args[0]).toAbsolutePath().normalize(),
                Stage.valueOf(args[1])
            );
    }
}
