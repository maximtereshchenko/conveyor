plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    api(project(":conveyor-api"))
    api(project(":conveyor-plugin-api"))
    testImplementation(project(":jackson-adapter"))
    testImplementation(project(":wiremock"))
    testImplementation(project(":jackson-dataformat-xml"))
    testImplementation(project(":jimfs-junit5-extension"))
    testImplementation(platform(libs.jackson.bom))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
    testImplementation(libs.jimfs)
}
