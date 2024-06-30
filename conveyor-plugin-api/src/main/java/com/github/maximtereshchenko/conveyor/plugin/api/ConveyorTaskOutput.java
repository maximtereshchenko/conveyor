package com.github.maximtereshchenko.conveyor.plugin.api;

public sealed interface ConveyorTaskOutput extends Comparable<ConveyorTaskOutput>
    permits PathConveyorTaskOutput {}
