module com.fasterxml.jackson.dataformat.xml.shadowed {
    requires java.xml;
    requires com.fasterxml.jackson.annotation;
    requires org.codehaus.stax2;
    exports com.fasterxml.jackson.dataformat.xml.shadowed;
    exports com.fasterxml.jackson.databind.shadowed;
    exports com.fasterxml.jackson.dataformat.xml.shadowed.annotation;
    exports com.fasterxml.jackson.annotation.shadowed;
}