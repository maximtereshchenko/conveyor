package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class ConveyorTasks {

    public static List<Path> executeTasks(List<ConveyorTask> tasks) {
        return tasks.stream()
            .map(ConveyorTask::action)
            .map(Supplier::get)
            .flatMap(Optional::stream)
            .toList();
    }
}
