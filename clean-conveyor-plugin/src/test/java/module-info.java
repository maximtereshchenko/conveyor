module com.github.maximtereshchenko.conveyor.plugin.clean.test {
    uses com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
    requires com.github.maximtereshchenko.conveyor.plugin.clean;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    requires com.google.common.jimfs;
    opens com.github.maximtereshchenko.conveyor.plugin.clean.test to org.junit.platform.commons;
}