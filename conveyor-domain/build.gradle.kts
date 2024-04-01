plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    api(project(":conveyor-api"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(project(":gson-conveyor-project-definition-reader"))
    testCompileOnly(project(":file-conveyor-plugin"))
}
