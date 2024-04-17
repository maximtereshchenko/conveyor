plugins {
    `conveyor-component`
    `test-conventions`
}

dependencies {
    testImplementation(project(":test-common"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.assertj.core)
    testImplementation(libs.jimfs)
}
