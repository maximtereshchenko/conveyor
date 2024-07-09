package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;

import java.util.Set;

public record ConveyorTask(
    String name,
    Stage stage,
    Step step,
    Runnable action,
    Set<ConveyorTaskInput> inputs,
    Set<ConveyorTaskOutput> outputs,
    Cache cache
) {}
