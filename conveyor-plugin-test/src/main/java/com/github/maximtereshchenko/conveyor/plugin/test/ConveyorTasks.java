package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class ConveyorTasks {

    public static List<Path> executeTasks(List<ConveyorTaskBinding> bindings) {
        return bindings.stream()
            .map(ConveyorTaskBinding::task)
            .map(ConveyorTask::execute)
            .flatMap(Optional::stream)
            .toList();
    }
}
