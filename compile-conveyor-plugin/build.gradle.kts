plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    implementation(project(":conveyor-plugin-api"))
    implementation(project(":compiler"))
    testImplementation(project(":test-common"))
    testImplementation(project(":conveyor-plugin-test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.assertj.core)
}
