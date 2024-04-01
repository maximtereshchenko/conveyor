package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;

public record ConveyorTaskBinding(Stage stage, Step step, ConveyorTask task) {}
