package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.util.List;

public final class ConveyorTasks {

    public static void executeTasks(List<ConveyorTask> tasks) {
        tasks.stream()
            .map(ConveyorTask::action)
            .forEach(Runnable::run);
    }
}
