module com.github.maximtereshchenko.conveyor.domain.test {
    requires java.compiler;
    requires com.github.maximtereshchenko.conveyor.api;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.domain;
    requires com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    opens com.github.maximtereshchenko.conveyor.domain.test to org.junit.platform.commons;
}