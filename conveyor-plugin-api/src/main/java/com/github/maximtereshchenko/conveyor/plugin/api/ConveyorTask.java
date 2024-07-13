package com.github.maximtereshchenko.conveyor.plugin.api;

import java.util.Set;

public record ConveyorTask(
    String name,
    BindingStage stage,
    BindingStep step,
    Runnable action,
    Set<ConveyorTaskInput> inputs,
    Set<ConveyorTaskOutput> outputs,
    Cache cache
) {}
