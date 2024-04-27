module com.github.maximtereshchenko.conveyor.plugin.resources.test {
    requires com.github.maximtereshchenko.conveyor.plugin.resources;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.test;
    requires com.github.maximtereshchenko.test.common;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;
    opens com.github.maximtereshchenko.conveyor.plugin.resources.test to org.junit.platform.commons;
}