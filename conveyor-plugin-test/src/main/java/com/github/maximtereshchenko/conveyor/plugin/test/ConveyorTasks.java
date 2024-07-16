package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.util.List;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

public final class ConveyorTasks {

    private final List<ConveyorTask> tasks;
    private final FakeConveyorSchematic schematic;

    ConveyorTasks(List<ConveyorTask> tasks, FakeConveyorSchematic schematic) {
        this.tasks = tasks;
        this.schematic = schematic;
    }

    public void contain(ConveyorTask... expected) {
        assertThat(tasks)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("action")
            .containsExactly(expected);
    }

    public ExecutionResult execute() {
        try {
            tasks.stream()
                .map(ConveyorTask::action)
                .forEach(action -> action.execute(new NoOpTracer()));
            return new Success(schematic);
        } catch (Exception e) {
            return new Failure(e);
        }
    }
}
