rootProject.name = "conveyor"
include("conveyor-api")
include("conveyor-domain")
include("conveyor-plugin-api")
include("gson-conveyor-project-definition-reader")
include("conveyor-common-api")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("assertj-core", "org.assertj", "assertj-core").version("3.25.1")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").version("5.10.1")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").version("5.10.1")
            library("gson", "com.google.code.gson", "gson").version("2.10.1")
        }
    }
}
