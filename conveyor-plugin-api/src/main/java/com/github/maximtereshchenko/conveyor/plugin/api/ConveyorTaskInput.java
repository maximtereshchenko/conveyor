package com.github.maximtereshchenko.conveyor.plugin.api;

public sealed interface ConveyorTaskInput extends Comparable<ConveyorTaskInput>
    permits KeyValueConveyorTaskInput, PathConveyorTaskInput {}
