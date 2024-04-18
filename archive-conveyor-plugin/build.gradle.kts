plugins {
    `conveyor-component`
    `test-conventions`
}

dependencies {
    implementation(project(":conveyor-plugin-api"))
    implementation(project(":zip-archive"))
    testImplementation(project(":test-common"))
    testImplementation(project(":conveyor-plugin-test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
}