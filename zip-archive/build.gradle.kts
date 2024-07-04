plugins {
    `conveyor-component`
    `test-conventions`
}

dependencies {
    implementation(project(":files"))
    testImplementation(project(":test-common"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.assertj.core)
}
