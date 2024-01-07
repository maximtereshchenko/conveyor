plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    api(project(":conveyor-api"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
