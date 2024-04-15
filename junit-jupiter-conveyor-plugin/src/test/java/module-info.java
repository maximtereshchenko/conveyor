module com.github.maximtereshchenko.conveyor.plugin.junit.jupiter.test {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.plugin.test;
    requires com.github.maximtereshchenko.test.common;
    requires com.github.maximtereshchenko.compiler;
    requires org.junit.jupiter.api;
    requires org.apiguardian.api;
    requires org.assertj.core;
    opens com.github.maximtereshchenko.conveyor.plugin.junit.jupiter.test to org.junit.platform.commons;
}