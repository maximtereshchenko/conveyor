plugins {
    `java-library`
}

dependencies {
    api(project(":conveyor-common-api"))
    api(project(":conveyor-plugin-api"))
    implementation(libs.assertj.core)
}
