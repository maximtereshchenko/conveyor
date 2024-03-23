module com.github.maximtereshchenko.conveyor.domain {
    requires java.xml;
    requires java.net.http;
    requires com.github.maximtereshchenko.conveyor.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    uses com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
    exports com.github.maximtereshchenko.conveyor.domain;
}