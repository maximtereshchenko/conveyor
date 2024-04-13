plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    testImplementation(project(":test-common"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.assertj.core)
    testImplementation(libs.jimfs)
}
