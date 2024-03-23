module com.github.maximtereshchenko.conveyor.jackson.dataformat.xml {
    requires java.xml;
    requires com.fasterxml.jackson.annotation;
    requires org.codehaus.stax2;
    exports com.github.maximtereshchenko.conveyor.jackson.dataformat.xml;
    exports com.github.maximtereshchenko.conveyor.jackson.databind;
    exports com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.annotation;
}