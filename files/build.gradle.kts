plugins {
    `conveyor-component`
    `test-conventions`
}

dependencies {
    testImplementation(project(":assertions"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
}
