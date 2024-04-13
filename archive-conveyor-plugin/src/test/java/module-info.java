module com.github.maximtereshchenko.conveyor.plugin.archive.test {
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.test;
    requires com.github.maximtereshchenko.test.common;
    requires com.github.maximtereshchenko.zip;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;
    opens com.github.maximtereshchenko.conveyor.plugin.archive.test to org.junit.platform.commons;
    uses com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
}