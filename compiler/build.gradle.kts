plugins {
    `conveyor-component`
    `test-conventions`
}

dependencies {
    implementation(project(":conveyor-plugin-api"))
    testImplementation(project(":assertions"))
    testImplementation(project(":files"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
}
