module com.github.maximtereshchenko.conveyor.core.test {
    requires com.github.maximtereshchenko.conveyor.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.core;
    requires com.github.maximtereshchenko.compiler;
    requires com.github.maximtereshchenko.zip;
    requires com.github.maximtereshchenko.conveyor.jackson;
    requires com.github.tomakehurst.wiremock.shadowed;
    requires com.fasterxml.jackson.dataformat.xml.shadowed;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;
    requires com.google.common.jimfs;
    requires com.github.maximtereshchenko.test.common;
    opens com.github.maximtereshchenko.conveyor.core.test to org.junit.platform.commons,
        com.fasterxml.jackson.dataformat.xml.shadowed;
}