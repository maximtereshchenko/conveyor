package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.BindingStage;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStep;

interface Task extends Comparable<Task> {

    String name();

    BindingStage stage();

    BindingStep step();

    void execute();
}
