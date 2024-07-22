plugins {
    `conveyor-component`
    `test-conventions`
}

dependencies {
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.platform.launcher)
}
