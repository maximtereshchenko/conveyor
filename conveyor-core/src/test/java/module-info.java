module com.github.maximtereshchenko.conveyor.core.test {
    requires java.compiler;
    requires com.github.maximtereshchenko.conveyor.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.core;
    requires com.github.maximtereshchenko.conveyor.jackson;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.tomakehurst.wiremock.shadowed;
    requires com.fasterxml.jackson.dataformat.xml.shadowed;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;
    requires com.google.common.jimfs;
    opens com.github.maximtereshchenko.conveyor.core.test to org.junit.platform.commons,
        com.fasterxml.jackson.dataformat.xml.shadowed;
}