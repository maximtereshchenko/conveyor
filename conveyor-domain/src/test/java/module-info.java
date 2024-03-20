module com.github.maximtereshchenko.conveyor.domain.test {
    requires java.compiler;
    requires com.github.maximtereshchenko.conveyor.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.domain;
    requires com.github.maximtereshchenko.conveyor.jackson;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    opens com.github.maximtereshchenko.conveyor.domain.test to org.junit.platform.commons;
}