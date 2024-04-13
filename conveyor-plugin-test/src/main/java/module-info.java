module com.github.maximtereshchenko.conveyor.plugin.test {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires org.assertj.core;
    uses com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
    exports com.github.maximtereshchenko.conveyor.plugin.test;
}