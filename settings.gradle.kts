rootProject.name = "conveyor"
include("conveyor-api")
include("conveyor-domain")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("assertj-core", "org.assertj", "assertj-core").version("3.25.1")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").version("5.10.1")
        }
    }
}
