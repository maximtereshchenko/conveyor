rootProject.name = "conveyor"
include("conveyor-api")
include("conveyor-domain")
include("conveyor-plugin-api")
include("jackson-adapter")
include("conveyor-common-api")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("assertj-core", "org.assertj", "assertj-core").version("3.25.1")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").version("5.10.1")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").version("5.10.1")
            library("jackson-bom", "com.fasterxml.jackson", "jackson-bom").version("2.16.1")
            library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind").withoutVersion()
            library("jackson-datatype-jdk8", "com.fasterxml.jackson.datatype", "jackson-datatype-jdk8").withoutVersion()
        }
    }
}
