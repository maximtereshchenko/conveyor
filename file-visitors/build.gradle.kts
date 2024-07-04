plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    testImplementation(project(":test-common"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
}
