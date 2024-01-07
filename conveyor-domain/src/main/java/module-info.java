module com.github.maximtereshchenko.conveyor.domain {
    requires com.github.maximtereshchenko.conveyor.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    uses com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
    exports com.github.maximtereshchenko.conveyor.domain;
}