plugins {
    `conveyor-component`
    `test-conventions`
}

dependencies {
    implementation(project(":conveyor-plugin-api"))
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.platform.launcher)
    implementation(libs.junit.jupiter.engine)
    testImplementation(project(":assertions"))
    testImplementation(project(":conveyor-plugin-test"))
    testImplementation(project(":compiler"))
    testImplementation(project(":files"))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.apiguardian.api)
}
