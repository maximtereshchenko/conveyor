module com.github.maximtereshchenko.conveyor.domain {
    requires com.github.maximtereshchenko.conveyor.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    uses com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
    opens com.github.maximtereshchenko.conveyor.domain to com.google.gson; //TODO
    exports com.github.maximtereshchenko.conveyor.domain;
}