module com.github.maximtereshchenko.conveyor.domain.test {
    requires java.compiler;
    requires com.github.maximtereshchenko.conveyor.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.domain;
    requires com.github.maximtereshchenko.conveyor.jackson;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.wiremock;
    requires com.github.maximtereshchenko.conveyor.jackson.dataformat.xml;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;
    opens com.github.maximtereshchenko.conveyor.domain.test to org.junit.platform.commons,
        com.github.maximtereshchenko.conveyor.jackson.dataformat.xml;
}