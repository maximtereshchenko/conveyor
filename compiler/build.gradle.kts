plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    implementation(project(":conveyor-plugin-api"))
    testImplementation(project(":test-common"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.assertj.core)
}
