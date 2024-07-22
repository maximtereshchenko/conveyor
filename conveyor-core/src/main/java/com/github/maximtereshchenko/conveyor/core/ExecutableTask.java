package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.BindingStage;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStep;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

final class ExecutableTask implements Task {

    private final ConveyorTask conveyorTask;
    private final Tracer tracer;

    ExecutableTask(ConveyorTask conveyorTask, Tracer tracer) {
        this.conveyorTask = conveyorTask;
        this.tracer = tracer;
    }

    @Override
    public String name() {
        return conveyorTask.name();
    }

    @Override
    public BindingStage stage() {
        return conveyorTask.stage();
    }

    @Override
    public BindingStep step() {
        return conveyorTask.step();
    }

    @Override
    public void execute() {
        tracer.submitTaskExecution(conveyorTask.name());
        conveyorTask.action()
            .execute((tracingImportance, supplier) ->
                tracer.submit(Importance.valueOf(tracingImportance.name()), supplier)
            );
    }

    @Override
    public int compareTo(Task task) {
        var byStage = stage().compareTo(task.stage());
        if (byStage == 0) {
            return step().compareTo(task.step());
        }
        return byStage;
    }
}
