plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    implementation(project(":conveyor-plugin-api"))
    implementation(project(":zip-archive"))
    testImplementation(project(":jimfs-junit5-extension"))
    testImplementation(project(":conveyor-plugin-test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.assertj.core)
}
