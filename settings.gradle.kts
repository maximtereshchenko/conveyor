rootProject.name = "conveyor"
include("conveyor-common-api")
include("conveyor-plugin-api")
include("conveyor-api")
include("conveyor-core")
include("jackson-adapter")
include("clean-conveyor-plugin")
include("compile-conveyor-plugin")
include("resources-conveyor-plugin")
include("test-common")
include("conveyor-plugin-test")
include("archive-conveyor-plugin")
include("zip-archive")
include("compiler")
include("junit-jupiter-conveyor-plugin")
include("conveyor-cli")
include("publish-conveyor-plugin")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("assertj-core", "org.assertj", "assertj-core").version("3.25.1")
            library("junit-bom", "org.junit", "junit-bom").version("5.10.2")
            library("jackson-bom", "com.fasterxml.jackson", "jackson-bom").version("2.17.0")
            library("wiremock", "org.wiremock", "wiremock").version("3.4.2")
            library("apiguardian-api", "org.apiguardian", "apiguardian-api").version("1.1.2")
            library("slf4j-jdk14", "org.slf4j", "slf4j-jdk14").version("2.0.13")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine")
                .withoutVersion()
            library("junit-jupiter-params", "org.junit.jupiter", "junit-jupiter-params")
                .withoutVersion()
            library("junit-platform-launcher", "org.junit.platform", "junit-platform-launcher")
                .withoutVersion()
            library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind")
                .withoutVersion()
            library("jackson-datatype-jdk8", "com.fasterxml.jackson.datatype", "jackson-datatype-jdk8")
                .withoutVersion()
            library("jackson-dataformat-xml", "com.fasterxml.jackson.dataformat", "jackson-dataformat-xml")
                .withoutVersion()
        }
    }
}
