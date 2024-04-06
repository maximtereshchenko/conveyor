rootProject.name = "conveyor"
include("conveyor-common-api")
include("conveyor-plugin-api")
include("conveyor-api")
include("conveyor-core")
include("jackson-adapter")
include("jackson-dataformat-xml")
include("wiremock")
include("clean-conveyor-plugin")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("assertj-core", "org.assertj", "assertj-core").version("3.25.1")
            library("junit-bom", "org.junit", "junit-bom").version("5.10.2")
            library("jackson-bom", "com.fasterxml.jackson", "jackson-bom").version("2.17.0")
            library("wiremock", "org.wiremock", "wiremock").version("3.4.2")
            library("jimfs", "com.google.jimfs", "jimfs").version("1.3.0")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").withoutVersion()
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine")
                .withoutVersion()
            library("junit-jupiter-params", "org.junit.jupiter", "junit-jupiter-params")
                .withoutVersion()
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind")
                .withoutVersion()
            library("jackson-datatype-jdk8", "com.fasterxml.jackson.datatype", "jackson-datatype-jdk8")
                .withoutVersion()
            library("jackson-dataformat-xml", "com.fasterxml.jackson.dataformat", "jackson-dataformat-xml")
                .withoutVersion()
            library("jackson-core", "com.fasterxml.jackson.core", "jackson-core")
                .withoutVersion()
            library("jackson-annotations", "com.fasterxml.jackson.core", "jackson-annotations")
                .withoutVersion()
        }
    }
}
