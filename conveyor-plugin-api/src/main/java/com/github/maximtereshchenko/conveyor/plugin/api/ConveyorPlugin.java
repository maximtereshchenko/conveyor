package com.github.maximtereshchenko.conveyor.plugin.api;

import java.util.List;
import java.util.Map;

public interface ConveyorPlugin {

    String name();

    List<ConveyorTaskBinding> bindings(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    );
}
