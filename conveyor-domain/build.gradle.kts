plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    api(project(":conveyor-api"))
    implementation(libs.gson)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
