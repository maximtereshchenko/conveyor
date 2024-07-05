plugins {
    `conveyor-component`
    `test-conventions`
}

dependencies {
    implementation(project(":files"))
    testImplementation(project(":assertions"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)

}
