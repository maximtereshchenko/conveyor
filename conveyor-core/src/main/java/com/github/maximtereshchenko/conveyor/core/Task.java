package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.BindingStage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;

import java.nio.file.Path;
import java.util.Set;

final class Task implements Comparable<Task> {

    private final ConveyorTask conveyorTask;
    private final TaskCache taskCache;
    private final Path directory;
    private final Tracer tracer;

    Task(
        ConveyorTask conveyorTask,
        TaskCache taskCache,
        Path directory,
        Tracer tracer
    ) {
        this.conveyorTask = conveyorTask;
        this.taskCache = taskCache;
        this.directory = directory;
        this.tracer = tracer;
    }

    @Override
    public int compareTo(Task task) {
        var byStage = conveyorTask.stage().compareTo(task.conveyorTask.stage());
        if (byStage == 0) {
            return conveyorTask.step().compareTo(task.conveyorTask.step());
        }
        return byStage;
    }

    @Override
    public String toString() {
        return conveyorTask.name();
    }

    void execute(Set<BindingStage> activeStages) {
        if (!activeStages.contains(conveyorTask.stage())) {
            return;
        }
        tracer.submitTaskExecution();
        action()
            .execute((tracingImportance, supplier, throwable) ->
                tracer.submit(Importance.valueOf(tracingImportance.name()), supplier, throwable)
            );
    }

    private ConveyorTaskAction action() {
        return switch (conveyorTask.cache()) {
            case ENABLED -> new CacheableAction(
                conveyorTask.action(),
                new Inputs(conveyorTask.inputs()),
                new Outputs(conveyorTask.outputs()),
                taskCache,
                directory,
                tracer
            );
            case DISABLED -> conveyorTask.action();
        };
    }
}
