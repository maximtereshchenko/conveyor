package com.github.maximtereshchenko.conveyor.plugin.api;

import java.util.function.Supplier;

public interface ConveyorTaskTracer {

    void submit(TracingImportance importance, Supplier<String> supplier);
}
