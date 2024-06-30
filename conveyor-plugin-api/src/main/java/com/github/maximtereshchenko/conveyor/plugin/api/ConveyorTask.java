package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public record ConveyorTask(
    String name,
    Stage stage,
    Step step,
    Supplier<Optional<Path>> action,
    Set<ConveyorTaskInput> inputs,
    Set<ConveyorTaskOutput> outputs,
    Cache cache
) {}
