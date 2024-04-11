plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    implementation(project(":conveyor-plugin-api"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
    testImplementation(libs.jimfs)
}
