module com.github.maximtereshchenko.conveyor.plugin.compile.test {
    requires com.github.maximtereshchenko.conveyor.plugin.compile;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.test;
    requires com.github.maximtereshchenko.jimfs;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    opens com.github.maximtereshchenko.conveyor.plugin.compile.test to org.junit.platform.commons;
    uses com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
}