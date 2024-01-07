plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    api(project(":conveyor-api"))
    implementation(project(":conveyor-plugin-api"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(project(":gson-conveyor-project-definition-reader"))
    testImplementation(project(":file-conveyor-plugin"))
}
