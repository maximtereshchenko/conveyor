plugins {
    `java-library`
}

dependencies {
    api(project(":conveyor-plugin-api"))
    implementation(libs.assertj.core)
}
