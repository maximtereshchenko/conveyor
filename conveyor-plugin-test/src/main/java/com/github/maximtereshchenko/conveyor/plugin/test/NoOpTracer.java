package com.github.maximtereshchenko.conveyor.plugin.test;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;

import java.util.function.Supplier;

final class NoOpTracer implements ConveyorTaskTracer {

    @Override
    public void submit(TracingImportance importance, Supplier<String> supplier) {
        //empty
    }
}
