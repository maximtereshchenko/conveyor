module com.github.maximtereshchenko.compiler.test {
    requires com.github.maximtereshchenko.compiler;
    requires com.github.maximtereshchenko.test.common;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    opens com.github.maximtereshchenko.compiler.test to org.junit.platform.commons;
}