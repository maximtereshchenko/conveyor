package com.github.maximtereshchenko.conveyor.plugin.api;

import java.util.Set;

public record ConveyorTask(
    String name,
    BindingStage stage,
    BindingStep step,
    ConveyorTaskAction action,
    Set<ConveyorTaskInput> inputs,
    Set<ConveyorTaskOutput> outputs,
    Cache cache
) {}
