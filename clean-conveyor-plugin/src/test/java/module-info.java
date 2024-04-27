module com.github.maximtereshchenko.conveyor.plugin.clean.test {
    requires com.github.maximtereshchenko.conveyor.plugin.clean;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.test;
    requires com.github.maximtereshchenko.test.common;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    requires org.junit.jupiter.params;
    opens com.github.maximtereshchenko.conveyor.plugin.clean.test to org.junit.platform.commons;
}