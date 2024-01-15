plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    implementation(project(":conveyor-plugin-api"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
}
