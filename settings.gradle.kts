rootProject.name = "conveyor"
include("conveyor-api")
include("conveyor-domain")
include("conveyor-plugin-api")
include("jackson-adapter")
include("conveyor-common-api")
include("wiremock")
include("jackson-dataformat-xml")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val version = version("asm", "9.6")
            library("assertj-core", "org.assertj", "assertj-core").version("3.25.1")
            library("junit-bom", "org.junit", "junit-bom").version("5.10.2")
            library("jackson-bom", "com.fasterxml.jackson", "jackson-bom").version("2.16.1")
            library("wiremock", "org.wiremock", "wiremock").version("3.4.2")
            library("asm", "org.ow2.asm", "asm").versionRef(version)
            library("asm-commons", "org.ow2.asm", "asm-commons").versionRef(version)
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
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.1")
        }
    }
}
